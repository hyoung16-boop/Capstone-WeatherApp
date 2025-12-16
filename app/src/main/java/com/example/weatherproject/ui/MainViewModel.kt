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

@HiltViewModel
class MainViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationProvider: LocationProvider,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    // [추가됨] 현재 GPS 위치를 자동으로 추적할지 여부를 결정하는 상태
    private val _isFollowingGps = MutableStateFlow(true)

    // 메인 날씨 상태 (UI가 바라보는 데이터)
    private val _uiState = MutableStateFlow(WeatherState())
    val uiState: StateFlow<WeatherState> = _uiState.asStateFlow()

    // 새로고침 상태
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // 체질 보정값
    private val _tempAdjustment = MutableStateFlow(0)
    val tempAdjustment: StateFlow<Int> = _tempAdjustment.asStateFlow()

    // 최초 설정 다이얼로그 표시 여부
    private val _showSetupDialog = MutableStateFlow(false)
    val showSetupDialog: StateFlow<Boolean> = _showSetupDialog.asStateFlow()

    // 체감온도 보정 다이얼로그 표시 여부 (설정 화면용)
    private val _showTempAdjustmentDialog = MutableStateFlow(false)
    val showTempAdjustmentDialog: StateFlow<Boolean> = _showTempAdjustmentDialog.asStateFlow()

    // 에러 메시지 (일회성 이벤트)
    private val _errorEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    val currentLocation: StateFlow<Location?> = locationProvider.currentLocation

    init {
        loadCachedWeather()
        checkUserPreference()
        observeLocationUpdates()
        // 앱 시작 시, GPS 추적 모드가 기본값이므로 위치 추적 시작
        startLocationTracking()
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationProvider.currentLocation.collect { location ->
                // [수정됨] GPS 추적 모드일 때만, 위치 변경에 따라 자동으로 날씨를 가져옴
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
                // [수정됨] GPS 추적 모드일 때만, 자동으로 주소를 업데이트함
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

    // LocationProvider에 위임
    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()
    fun getCurrentLocationOnce() = locationProvider.getCurrentLocationOnce()
    fun startLocationTracking() = locationProvider.startLocationTracking()
    private fun stopLocationTracking() = locationProvider.stopLocationTracking()


    private suspend fun fetchWeatherFromServer(lat: Double, lon: Double) {
        weatherRepository.getWeatherData(lat, lon, _tempAdjustment.value)
            .onSuccess { newWeatherState ->
                // 주소, 위도, 경도는 이 함수에서 건드리지 않고, 날씨 관련 데이터만 업데이트합니다.
                _uiState.value = _uiState.value.copy(
                    isLoading = newWeatherState.isLoading,
                    currentWeather = newWeatherState.currentWeather,
                    weatherDetails = newWeatherState.weatherDetails,
                    hourlyForecast = newWeatherState.hourlyForecast,
                    weeklyForecast = newWeatherState.weeklyForecast,
                    lastUpdated = newWeatherState.lastUpdated,
                    error = null // 성공 시 에러 메시지 제거
                )
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
                // 현재 GPS 추적 모드라면, 현재 위치 기준 새로고침
                if (_isFollowingGps.value) {
                    getCurrentLocationOnce()
                } else { // 검색 위치 고정 모드라면, 현재 표시중인 좌표 기준 새로고침
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

    // [수정됨] '현재 위치' 버튼을 눌렀을 때의 동작
    fun refreshMyLocation() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            
            // 1. GPS 추적 모드로 전환
            _isFollowingGps.value = true
            // 2. 위치 추적을 다시 시작
            startLocationTracking()
            // 3. 즉시 현재 위치를 한번 가져와서 업데이트 플로우를 트리거
            getCurrentLocationOnce()
            
            // 로딩 표시는 위의 로직들이 실행된 후 일정시간 뒤 해제
            kotlinx.coroutines.delay(1500) // 사용자가 인지할 시간을 줌
            _isRefreshing.value = false
        }
    }

    // [수정됨] 다른 지역을 검색했을 때의 동작
    fun updateWeatherByLocation(city: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            // 1. GPS 추적 모드를 비활성화하여, 백그라운드 업데이트가 검색 결과를 덮어쓰지 않도록 함
            _isFollowingGps.value = false
            stopLocationTracking()

            _isRefreshing.value = true
            // 2. 검색된 위치의 주소와 좌표로 UI 상태를 명시적으로 업데이트
            _uiState.value = _uiState.value.copy(address = city, latitude = lat, longitude = lon)
            try {
                // 3. 해당 위치의 날씨 정보를 가져옴
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