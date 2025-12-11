package com.example.weatherproject.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherproject.data.CctvInfo
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.network.RetrofitClient
import com.example.weatherproject.network.CurrentWeatherResponse
import com.example.weatherproject.network.HourlyForecastResponse
import com.example.weatherproject.network.WeeklyForecastResponse
import com.example.weatherproject.util.FeelsLikeTempCalculator
import com.example.weatherproject.util.GpsTransfer
import com.example.weatherproject.util.PreferenceManager
import com.google.android.gms.location.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(
    application: Application,
    private val weatherRepository: com.example.weatherproject.data.repository.WeatherRepository,
    private val locationProvider: com.example.weatherproject.util.LocationProvider
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val preferenceManager = PreferenceManager(application)

    // 메인 날씨 상태 (UI가 바라보는 데이터)
    private val _uiState = MutableStateFlow(WeatherState())
    val uiState: StateFlow<WeatherState> = _uiState

    // 새로고침 상태
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    // 체질 보정값
    private val _tempAdjustment = MutableStateFlow(0)
    val tempAdjustment = _tempAdjustment.asStateFlow()

    // 최초 설정 다이얼로그 표시 여부
    private val _showSetupDialog = MutableStateFlow(false)
    val showSetupDialog = _showSetupDialog.asStateFlow()

    // 체감온도 보정 다이얼로그 표시 여부 (설정 화면용)
    private val _showTempAdjustmentDialog = MutableStateFlow(false)
    val showTempAdjustmentDialog = _showTempAdjustmentDialog.asStateFlow()

    // 에러 메시지 (일회성 이벤트)
    private val _errorEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    val currentLocation: StateFlow<Location?> = locationProvider.currentLocation

    init {
        loadCachedWeather()
        checkUserPreference()
        observeLocationUpdates()
    }

    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationProvider.currentLocation.collect { location ->
                location?.let {
                    _uiState.value = _uiState.value.copy(latitude = it.latitude, longitude = it.longitude)
                    fetchWeatherFromServer(it.latitude, it.longitude)
                }
            }
        }
        viewModelScope.launch {
            locationProvider.address.collect { address ->
                if (address.isNotBlank()) {
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
    fun stopLocationTracking() = locationProvider.stopLocationTracking()


    private suspend fun fetchWeatherFromServer(lat: Double, lon: Double) {
        weatherRepository.getWeatherData(lat, lon, _tempAdjustment.value)
            .onSuccess { newWeatherState ->
                // 주소와 위치 정보는 LocationProvider가 업데이트하므로, 여기서는 날씨 정보만 합칩니다.
                _uiState.value = newWeatherState.copy(
                    address = _uiState.value.address,
                    latitude = _uiState.value.latitude,
                    longitude = _uiState.value.longitude
                )
            }
            .onFailure { error ->
                Log.e(TAG, "getWeatherData 실패: ${error.message}", error)
                _errorEvent.emit("날씨 정보를 가져올 수 없습니다.")
            }
    }

    fun refreshData() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            try {
                // 현재 위치로 날씨 다시 가져오기
                locationProvider.currentLocation.value?.let { location ->
                    fetchWeatherFromServer(location.latitude, location.longitude)
                } ?: run {
                    getCurrentLocationOnce()
                }
            } catch (e: Exception) {
                _errorEvent.emit(e.message ?: "알 수 없는 오류가 발생했습니다.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshMyLocation() {
        getCurrentLocationOnce()
    }

    fun updateWeatherByLocation(city: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            // 검색된 위치로 날씨를 업데이트할 때, 주소는 직접 설정
            _uiState.value = _uiState.value.copy(address = city, latitude = lat, longitude = lon)
            fetchWeatherFromServer(lat, lon)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}