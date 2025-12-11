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
    private val weatherRepository: com.example.weatherproject.data.repository.WeatherRepository // Repository 주입
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

    // 위치 관련
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)


    private var locationCallback: LocationCallback? = null

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    private val _isTrackingLocation = MutableStateFlow(false)
    val isTrackingLocation: StateFlow<Boolean> = _isTrackingLocation

    private val geocoder = Geocoder(application, Locale.KOREAN)

    init {
        loadCachedWeather() // 1. 시작할 때 캐시된 데이터 먼저 로드
        checkUserPreference()
    }

    // 캐시된 날씨 정보 로드
    private fun loadCachedWeather() {
        viewModelScope.launch {
            // Repository를 통해 캐시된 데이터 로드
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
        _showSetupDialog.value = false // 초기 설정 다이얼로그 닫기
        _showTempAdjustmentDialog.value = false // 재설정 다이얼로그 닫기

        // 값이 변경되었으므로, 현재 날씨 정보가 있다면 체감온도를 즉시 재계산하고 UI를 업데이트합니다.
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

    // GPS가 비활성화되었을 때 호출될 함수
    fun onGpsDisabled() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            address = "GPS를 켜서 현재 위치 날씨를 확인하세요."
        )
    }

    // 위치 권한 확인
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 한 번만 위치 가져오기
    fun getCurrentLocationOnce() {
        if (!hasLocationPermission()) {
            return
        }

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _currentLocation.value = it
                    getAddressFromLocation(it.latitude, it.longitude)

                    // 위치를 받으면 즉시 날씨 데이터 가져오기
                    viewModelScope.launch {
                        fetchWeatherFromServer(it.latitude, it.longitude)
                    }

                    // UI 상태에도 위도/경도 반영
                    _uiState.value = _uiState.value.copy(
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            viewModelScope.launch {
                _errorEvent.emit("위치 정보를 가져올 수 없습니다.")
            }
        }
    }

    // 실시간 위치 추적 시작
    fun startLocationTracking() {
        if (!hasLocationPermission()) {
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            60000L // 60초마다 업데이트
        ).apply {
            setMinUpdateIntervalMillis(30000L) // 최소 30초
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    _currentLocation.value = location
                    getAddressFromLocation(location.latitude, location.longitude)

                    // 위치 업데이트되면 날씨도 업데이트
                    viewModelScope.launch {
                        fetchWeatherFromServer(location.latitude, location.longitude)
                    }

                    // UI 상태에도 위도/경도 반영
                    _uiState.value = _uiState.value.copy(
                        latitude = location.latitude,
                        longitude = location.longitude
                    )
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            _isTrackingLocation.value = true
        } catch (e: SecurityException) {
            e.printStackTrace()
            viewModelScope.launch {
                _errorEvent.emit("위치 추적을 시작할 수 없습니다.")
            }
        }
    }

    // 위치 추적 중지
    fun stopLocationTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        _isTrackingLocation.value = false
    }

    // 위도/경도 → 주소 변환
    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val address = withContext(Dispatchers.IO) {
                    try {
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            
                            // 주소 파싱 로직은 그대로 유지
                            buildString {
                                // 시/도
                                addr.adminArea?.let {
                                    val simplified = it.replace("특별시", "시")
                                        .replace("광역시", "시")
                                        .replace("특별자치시", "시")
                                        .replace("특별자치도", "도")
                                    append(simplified)
                                }

                                // 구/군
                                val district = addr.subLocality ?: addr.locality ?: addr.subAdminArea
                                if (district != null) {
                                    append(" ")
                                    append(district)
                                }

                                // 동/읍/면
                                val neighborhood = addr.thoroughfare ?: addr.subThoroughfare
                                if (neighborhood != null) {
                                    append(" ")
                                    append(neighborhood)
                                }
                            }
                        } else {
                            "위치 정보 없음"
                        }
                    } catch (e: IOException) {
                        Log.e("Geocoder", "에러: ${e.message}")
                        "위치 확인 중..."
                    }
                }
                _uiState.value = _uiState.value.copy(address = address)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(address = "위치 확인 실패")
            }
        }
    }

    // Repository를 사용하도록 수정된 함수
    private suspend fun fetchWeatherFromServer(lat: Double, lon: Double) {
        // Repository에 날씨 데이터 요청 위임
        weatherRepository.getWeatherData(lat, lon, _tempAdjustment.value)
            .onSuccess { newWeatherState ->
                // 성공 시 UI 상태 업데이트
                _uiState.value = newWeatherState.copy(
                    // 주소와 위치 정보는 ViewModel이 계속 관리
                    address = _uiState.value.address,
                    latitude = lat,
                    longitude = lon
                )
            }
            .onFailure { error ->
                // 실패 시 에러 이벤트 발생
                Log.e(TAG, "getWeatherData 실패: ${error.message}", error)
                _errorEvent.emit("날씨 정보를 가져올 수 없습니다: ${error.message}")
            }
    }

    // 날씨 및 위치 데이터 통합 새로고침
    fun refreshData() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true

            try {
                // 현재 위치로 날씨 다시 가져오기
                _currentLocation.value?.let { location ->
                    fetchWeatherFromServer(location.latitude, location.longitude)
                } ?: run {
                    // 위치 정보가 없으면 위치부터 다시 요청
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
            _uiState.value = _uiState.value.copy(address = city, latitude = lat, longitude = lon)
            fetchWeatherFromServer(lat, lon)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}