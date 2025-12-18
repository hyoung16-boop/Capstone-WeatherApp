package com.example.weatherproject.ui

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.repository.WeatherRepository
import com.example.weatherproject.util.LocationProvider
import com.example.weatherproject.util.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 앱의 메인 화면(HomeScreen)에 대한 비즈니스 로직과 UI 상태를 관리하는 ViewModel입니다.
 *
 * 주요 역할:
 * 1. 위치 정보(GPS)를 추적하고, 해당 위치의 날씨 데이터를 서버에서 가져옵니다.
 * 2. 사용자가 검색한 지역의 날씨 정보를 업데이트합니다.
 * 3. 앱 설정(체감온도 보정 등)을 관리하고 반영합니다.
 * 4. UI 상태(로딩, 에러, 날씨 데이터)를 `StateFlow`로 노출하여 UI가 반응하도록 합니다.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _isFollowingGps = MutableStateFlow(true)

    private val _uiState = MutableStateFlow(WeatherState())
    val uiState: StateFlow<WeatherState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _tempAdjustment = MutableStateFlow(0)
    val tempAdjustment: StateFlow<Int> = _tempAdjustment.asStateFlow()

    private val _showSetupDialog = MutableStateFlow(false)
    val showSetupDialog: StateFlow<Boolean> = _showSetupDialog.asStateFlow()

    private val _showTempAdjustmentDialog = MutableStateFlow(false)
    val showTempAdjustmentDialog: StateFlow<Boolean> = _showTempAdjustmentDialog.asStateFlow()

    private val _errorEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    val currentLocation: StateFlow<Location?> = locationProvider.currentLocation

    init {
        loadCachedWeather()
        checkUserPreference()
        observeLocationUpdates()
        startLocationTracking()
    }

    /**
     * 위치 제공자(LocationProvider)로부터 실시간 위치 업데이트를 구독합니다.
     * GPS 추적 모드(_isFollowingGps)가 활성화된 경우에만, 변경된 위치 기반으로 날씨를 갱신합니다.
     */
    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationProvider.currentLocation.collect { location ->
                if (_isFollowingGps.value) {
                    location?.let {
                        _uiState.value = _uiState.value.copy(latitude = it.latitude, longitude = it.longitude)
                        fetchWeatherFromServer(it.latitude, it.longitude)
                    }
                }
            }
        }
        viewModelScope.launch {
            locationProvider.address.collect { address ->
                if (_isFollowingGps.value && address.isNotBlank()) {
                    _uiState.value = _uiState.value.copy(address = address)
                }
            }
        }
    }

    private fun loadCachedWeather() {
        viewModelScope.launch {
            val cachedWeather = weatherRepository.getCachedWeather()
            if (cachedWeather != null) {
                _uiState.value = cachedWeather.copy(isLoading = false)
                preferenceManager.saveWeatherState(cachedWeather)
            }
        }
    }

    private fun checkUserPreference() {
        if (!preferenceManager.isSetupComplete()) {
            _showSetupDialog.value = true
        } else {
            _tempAdjustment.value = preferenceManager.getTempAdjustment()
        }
    }

    fun saveTempAdjustment(value: Int) {
        preferenceManager.setTempAdjustment(value)
        _tempAdjustment.value = value
        _showSetupDialog.value = false
        _showTempAdjustmentDialog.value = false

        viewModelScope.launch {
            uiState.value.latitude?.let { lat ->
                uiState.value.longitude?.let { lon ->
                    fetchWeatherFromServer(lat, lon)
                }
            }
        }
    }

    fun openTempAdjustmentDialog() {
        _showTempAdjustmentDialog.value = true
    }

    fun closeTempAdjustmentDialog() {
        _showTempAdjustmentDialog.value = false
    }

    fun onGpsDisabled() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            address = "GPS를 켜서 현재 위치 날씨를 확인하세요."
        )
    }

    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()
    fun getCurrentLocationOnce() = locationProvider.getCurrentLocationOnce()
    fun startLocationTracking() = locationProvider.startLocationTracking()
    private fun stopLocationTracking() = locationProvider.stopLocationTracking()


    /**
     * 서버로부터 날씨 데이터를 비동기로 요청하고 UI 상태를 업데이트합니다.
     * 성공 시 데이터를 로컬 DB 및 Preference에 캐싱하며, 실패 시 에러 상태를 UI에 전달합니다.
     */
    private suspend fun fetchWeatherFromServer(lat: Double, lon: Double) {
        weatherRepository.getWeatherData(lat, lon, _tempAdjustment.value)
            .onSuccess { newWeatherState ->
                val updatedState = _uiState.value.copy(
                    isLoading = newWeatherState.isLoading,
                    currentWeather = newWeatherState.currentWeather,
                    weatherDetails = newWeatherState.weatherDetails,
                    hourlyForecast = newWeatherState.hourlyForecast,
                    weeklyForecast = newWeatherState.weeklyForecast,
                    lastUpdated = newWeatherState.lastUpdated,
                    error = null
                )
                _uiState.value = updatedState
                preferenceManager.saveWeatherState(updatedState)
            }
            .onFailure { error ->
                Log.e(TAG, "getWeatherData 실패: ${error.message}", error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "날씨 정보를 가져올 수 없습니다.\n네트워크 연결을 확인해주세요."
                )
            }
    }

    fun refreshData() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            try {
                if (_isFollowingGps.value) {
                    getCurrentLocationOnce()
                } else {
                    uiState.value.latitude?.let { lat ->
                        uiState.value.longitude?.let { lon ->
                            fetchWeatherFromServer(lat, lon)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorEvent.emit(e.message ?: "알 수 없는 오류가 발생했습니다.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    /**
     * 사용자가 '현재 위치' 버튼을 눌렀을 때 호출됩니다.
     * 검색 모드를 종료하고 GPS 추적 모드로 전환하며, 강제로 위치 및 날씨 정보를 갱신합니다.
     * StateFlow의 중복 무시 특성을 우회하기 위해 강제 호출 로직이 포함되어 있습니다.
     */
    fun refreshMyLocation() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            
            _isFollowingGps.value = true
            startLocationTracking()
            getCurrentLocationOnce()

            currentLocation.value?.let { loc ->
                fetchWeatherFromServer(loc.latitude, loc.longitude)
            }
            
            val currentAddr = locationProvider.address.value
            if (currentAddr.isNotBlank()) {
                _uiState.value = _uiState.value.copy(address = currentAddr)
            }
            
            kotlinx.coroutines.delay(1500)
            _isRefreshing.value = false
        }
    }

    /**
     * 사용자가 특정 도시를 검색했을 때 호출됩니다.
     * GPS 추적을 중단하고, 선택된 도시의 좌표로 날씨 정보를 수동 업데이트합니다.
     */
    fun updateWeatherByLocation(city: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _isFollowingGps.value = false
            stopLocationTracking()

            _isRefreshing.value = true
            _uiState.value = _uiState.value.copy(address = city, latitude = lat, longitude = lon)
            try {
                fetchWeatherFromServer(lat, lon)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}
