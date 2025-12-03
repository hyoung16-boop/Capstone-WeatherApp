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

class MainViewModel(application: Application) : AndroidViewModel(application) {

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
            val cachedWeather = preferenceManager.getWeatherState()
            if (cachedWeather != null) {
                _uiState.value = cachedWeather.copy(isLoading = false) // 로딩 상태는 false로 시작
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
                    fetchWeatherFromServer(it.latitude, it.longitude)

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
                    fetchWeatherFromServer(location.latitude, location.longitude)

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

                                // 주소가 비어있으면 전체 주소에서 추출
                                if (isEmpty() || length < 5) {
                                    val fullAddress = addr.getAddressLine(0)
                                    if (fullAddress != null) {
                                        val parts = fullAddress.split(" ")
                                        val result = mutableListOf<String>()

                                        for (part in parts) {
                                            when {
                                                part.contains("특별시") || part.contains("광역시") || part.endsWith("시") -> {
                                                    result.add(part.replace("특별시", "시")
                                                        .replace("광역시", "시")
                                                        .replace("특별자치시", "시"))
                                                }
                                                part.endsWith("구") || part.endsWith("군") -> {
                                                    result.add(part)
                                                }
                                                part.endsWith("동") || part.endsWith("읍") || part.endsWith("면") -> {
                                                    result.add(part)
                                                    break
                                                }
                                            }
                                        }

                                        if (result.isNotEmpty()) {
                                            clear()
                                            append(result.joinToString(" "))
                                        }
                                    }
                                }

                                // 그래도 없으면 최소한 시/도라도
                                if (isEmpty()) {
                                    addr.adminArea?.let {
                                        append(it.replace("특별시", "시")
                                            .replace("광역시", "시")
                                            .replace("특별자치시", "시")
                                            .replace("특별자치도", "도"))
                                    }
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

                // 최종 주소
                val finalAddress = if (address.isBlank() || address == "위치 정보 없음") {
                    "위치: ${String.format("%.4f", latitude)}, ${String.format("%.4f", longitude)}"
                } else {
                    address
                }

                _uiState.value = _uiState.value.copy(address = finalAddress)

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(address = "위치 확인 실패")
            }
        }
    }

    // 서버에서 날씨 데이터 가져오기
    private fun fetchWeatherFromServer(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                // 1. GPS 좌표 → 격자 좌표 변환
                val (nx, ny) = GpsTransfer.convertToGrid(lat, lon)
                Log.d(TAG, "GPS($lat, $lon) → Grid($nx, $ny)")

                // 2. 현재 날씨 API 호출
                val currentResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.weatherApi.getCurrentWeather(nx, ny)
                }

                // 🔍 서버 응답 로그
                Log.d(TAG, "========================================")
                Log.d(TAG, "서버 응답 전체: $currentResponse")
                Log.d(TAG, "기온: ${currentResponse?.weather?.temp}°C")
                Log.d(TAG, "습도: ${currentResponse?.weather?.humidity}%")
                Log.d(TAG, "하늘 상태: ${currentResponse?.weather?.skyCondition}")
                Log.d(TAG, "강수 형태: ${currentResponse?.weather?.precipitationType}")
                Log.d(TAG, "최고기온: ${currentResponse?.weather?.maxTemp}°C")
                Log.d(TAG, "최저기온: ${currentResponse?.weather?.minTemp}°C")
                Log.d(TAG, "========================================")

                // 3. 시간별 예보 API 호출
                val hourlyResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.weatherApi.getHourlyForecast(nx, ny)
                }

                // 4. 주간 예보 API 호출
                val weeklyResponse = withContext(Dispatchers.IO) {
                    RetrofitClient.weatherApi.getWeeklyForecast(nx, ny)
                }

                // 5. 데이터 변환 및 UI 업데이트
                updateUiWithServerData(currentResponse, hourlyResponse, weeklyResponse)

            } catch (e: Exception) {
                Log.e(TAG, "API 호출 실패: ${e.message}", e)
                _errorEvent.emit("날씨 정보를 가져올 수 없습니다: ${e.message}")
            }
        }
    }

    // 서버 응답 데이터를 UI 상태로 변환
    private fun updateUiWithServerData(
        currentData: CurrentWeatherResponse?,
        hourlyData: HourlyForecastResponse?,
        weeklyData: WeeklyForecastResponse?
    ) {
        try {
            val weather = currentData?.weather

            // ⭐️ 체감온도 계산
            val temp = weather?.temp ?: 0.0
            val humidity = weather?.humidity ?: 0.0
            val windSpeedMs = weather?.windSpeed ?: 0.0
            val windSpeedKmh = windSpeedMs * 3.6

            val calculatedFeelsLike = FeelsLikeTempCalculator.calculate(temp, humidity, windSpeedKmh)
            val finalFeelsLike = calculatedFeelsLike + _tempAdjustment.value

            val feelsLikeString = "${finalFeelsLike.toInt()}°"

            // 현재 날씨 변환
            val currentWeather = CurrentWeather(
                iconUrl = getWeatherIconUrl(weather?.skyCondition ?: "맑음", weather?.precipitationType ?: "없음"),
                temperature = "${weather?.temp?.toInt() ?: 0}°",
                description = weather?.skyCondition ?: "정보 없음",
                maxTemp = "${weather?.maxTemp?.toInt() ?: 0}°",
                minTemp = "${weather?.minTemp?.toInt() ?: 0}°",
                feelsLike = feelsLikeString
            )

            // 상세 날씨 변환
            val weatherDetails = WeatherDetails(
                feelsLike = feelsLikeString,
                humidity = "${weather?.humidity?.toInt() ?: 0}%",
                precipitation = "${weather?.rainfall ?: 0.0} mm",
                wind = "${weather?.windSpeed ?: 0.0} m/s",
                pm10 = weather?.pm10?.trim() ?: "정보없음",
                pm25 = weather?.pm25?.trim() ?: "정보없음",
                pressure = "1013 hPa",
                visibility = "10 km",
                uvIndex = "5"
            )

            // 시간별 예보 변환
            val hourlyForecast = hourlyData?.weather?.take(24)?.map { item ->
                HourlyForecast(
                    time = formatTime(item.time),
                    iconUrl = getWeatherIconUrl(item.sky, item.pty),
                    temperature = "${item.temp?.toInt() ?: 0}°"
                )
            } ?: emptyList()

            // 주간 예보 변환
            val weeklyForecast = weeklyData?.weather?.map { item ->
                WeeklyForecast(
                    day = formatDate(item.date),
                    iconUrl = getWeatherIconUrl(item.skyAm, "없음"),
                    maxTemp = "${item.maxTemp?.toInt() ?: 0}°",
                    minTemp = "${item.minTemp?.toInt() ?: 0}°"
                )
            } ?: emptyList()

            // UI 상태 업데이트
            val lastUpdatedTimestamp = SimpleDateFormat("MM월 dd일 HH:mm", Locale.KOREAN).format(Date())
            val newState = _uiState.value.copy(
                isLoading = false,
                currentWeather = currentWeather,
                weatherDetails = weatherDetails,
                hourlyForecast = hourlyForecast,
                weeklyForecast = weeklyForecast,
                lastUpdated = "업데이트: $lastUpdatedTimestamp"
            )
            _uiState.value = newState
            preferenceManager.saveWeatherState(newState) // 2. 성공 시 새로운 데이터 캐시

            Log.d(TAG, "날씨 데이터 업데이트 완료")
        } catch (e: Exception) {
            Log.e(TAG, "날씨 데이터 변환 실패", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false
            )
        }
    }

    // 날씨 상태에 따른 아이콘 URL 반환
    private fun getWeatherIconUrl(sky: String, pty: String): String {
        return when {
            pty.contains("비") || pty.contains("소나기") -> "https://openweathermap.org/img/wn/10d@2x.png"
            pty.contains("눈") -> "https://openweathermap.org/img/wn/13d@2x.png"
            sky.contains("맑음") -> "https://openweathermap.org/img/wn/01d@2x.png"
            sky.contains("구름조금") || sky.contains("구름많음") -> "https://openweathermap.org/img/wn/02d@2x.png"
            sky.contains("흐림") -> "https://openweathermap.org/img/wn/03d@2x.png"
            else -> "https://openweathermap.org/img/wn/01d@2x.png"
        }
    }

    // 시간 포맷 (0900 → 09:00)
    private fun formatTime(time: String): String {
        return if (time.length == 4) {
            "${time.substring(0, 2)}:${time.substring(2, 4)}"
        } else {
            time
        }
    }

    // 날짜 포맷 (20231128 → 11/28 (화))
    private fun formatDate(date: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
            val parsedDate = sdf.parse(date)
            val outputFormat = SimpleDateFormat("MM/dd (E)", Locale.KOREAN)
            outputFormat.format(parsedDate ?: date)
        } catch (e: Exception) {
            date
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
        fetchWeatherFromServer(lat, lon)
        val currentState = _uiState.value
        _uiState.value = currentState.copy(address = city, latitude = lat, longitude = lon)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }
}