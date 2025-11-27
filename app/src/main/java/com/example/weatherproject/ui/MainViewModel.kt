package com.example.weatherproject.ui

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.util.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.weatherproject.data.CctvInfo

class MainViewModel(application: Application) : AndroidViewModel(application) {

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

    init {
        loadFakeData()
        checkUserPreference()
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

    // 날씨 및 위치 데이터 통합 새로고침
    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            
            // 1. 위치 갱신 시늉 (실제로는 GPS 요청)
            val currentState = _uiState.value
            _uiState.value = currentState.copy(currentAddress = "위치 재탐색 중...")
            
            kotlinx.coroutines.delay(1000) // GPS 딜레이
            
            _uiState.value = currentState.copy(currentAddress = "서울, 대한민국 (갱신됨)")

            // 2. 날씨 API 호출 시늉 (실제로는 서버 요청)
            kotlinx.coroutines.delay(500) // API 딜레이
            
            // 랜덤하게 온도 조금 바꿔서 갱신된 느낌 주기
            val current = _uiState.value.currentWeather
            val newTemp = (current.temperature.replace("°", "").toInt() + (-1..1).random()).toString() + "°"
            
            _uiState.value = _uiState.value.copy(
                currentWeather = current.copy(temperature = newTemp)
            )
            
            _isRefreshing.value = false
        }
    }

    fun refreshMyLocation() {
        refreshData()
    }

    fun updateWeatherByLocation(city: String, lat: Double, lon: Double) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(currentAddress = city)
        Log.d("MainViewModel", "Weather update requested for: $city ($lat, $lon)")
        // TODO: API Call
    }

    private fun loadFakeData() {
        // OpenWeather 공식 아이콘 주소
        val iconSunny = "http://openweathermap.org/img/wn/01d@2x.png"
        val iconPartlyCloudy = "http://openweathermap.org/img/wn/02d@2x.png"
        val iconCloudy = "http://openweathermap.org/img/wn/03d@2x.png"
        val iconRain = "http://openweathermap.org/img/wn/10d@2x.png"

        _uiState.value = WeatherState(
            currentAddress = "서울, 대한민국",

            currentWeather = CurrentWeather(
                iconUrl = iconPartlyCloudy,
                temperature = "18°",
                description = "Partly Cloudy",
                maxTemp = "최고: 22°",
                minTemp = "14°",
                feelsLike = "체감: 17°"
            ),

            weatherDetails = WeatherDetails(
                feelsLike = "17°",
                humidity = "65%",
                precipitation = "0 mm",
                wind = "12 km/h",
                pm10 = "45 µg/m³",
                pm25 = "18 µg/m³",
                pressure = "1013 hPa",
                visibility = "10 km",
                uvIndex = "5"
            ),

            hourlyForecast = listOf(
                HourlyForecast("지금", iconPartlyCloudy, "18°", "0mm", "보통"),
                HourlyForecast("15:00", iconSunny, "20°", "0mm", "보통"),
                HourlyForecast("16:00", iconSunny, "21°", "0mm", "보통"),
                HourlyForecast("17:00", iconSunny, "21°", "0mm", "보통"),
                HourlyForecast("18:00", iconCloudy, "19°", "10mm", "보통"),
                HourlyForecast("19:00", iconRain, "17°", "20mm", "좋음"),
                HourlyForecast("20:00", iconRain, "16°", "30mm", "좋음"),
                HourlyForecast("21:00", iconCloudy, "15°", "10mm", "좋음"),
                HourlyForecast("22:00", iconPartlyCloudy, "14°", "0mm", "좋음"),
                HourlyForecast("23:00", iconPartlyCloudy, "13°", "0mm", "좋음"),
                HourlyForecast("00:00", iconPartlyCloudy, "12°", "0mm", "좋음"),
                HourlyForecast("01:00", iconPartlyCloudy, "11°", "0mm", "좋음"),
                HourlyForecast("02:00", iconPartlyCloudy, "10°", "0mm", "좋음"),
                HourlyForecast("03:00", iconSunny, "9°", "0mm", "좋음"),
                HourlyForecast("04:00", iconSunny, "8°", "0mm", "좋음"),
                HourlyForecast("05:00", iconSunny, "8°", "0mm", "좋음"),
                HourlyForecast("06:00", iconPartlyCloudy, "9°", "0mm", "보통"),
                HourlyForecast("07:00", iconSunny, "11°", "0mm", "보통")
            ),

            weeklyForecast = listOf(
                WeeklyForecast(date = "11/17 (월)", iconSunny, "미세먼지 보통", "0mm", "15°", "23°"),
                WeeklyForecast(date = "11/18 (화)", iconPartlyCloudy, "미세먼지 나쁨", "0mm", "16°", "24°"),
                WeeklyForecast(date = "11/19 (수)", iconCloudy, "미세먼지 나쁨", "0mm", "14°", "22°"),
                WeeklyForecast(date = "11/20 (목)", iconRain, "미세먼지 좋음", "25mm", "13°", "20°"),
                WeeklyForecast(date = "11/21 (금)", iconRain, "미세먼지 좋음", "40mm", "12°", "19°"),
                WeeklyForecast(date = "11/22 (토)", iconSunny, "미세먼지 보통", "0mm", "13°", "22°"),
                WeeklyForecast(date = "11/23 (일)", iconPartlyCloudy, "미세먼지 보통", "0mm", "15°", "24°")
            ),
            
            // ⭐️ 근처 CCTV (더미 데이터 12개)
            cctvList = listOf(
                CctvInfo("1", "강남대로 (신논현역)", "0.2km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("2", "테헤란로 (역삼역)", "0.8km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("3", "경부고속도로 (반포IC)", "1.5km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("4", "올림픽대로 (청담대교)", "2.3km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("5", "강변북로 (동작대교)", "3.1km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("6", "잠실대교 남단", "4.5km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("7", "한남대교 북단", "5.2km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("8", "성수대교 남단", "5.8km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("9", "영동대교 북단", "6.1km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("10", "청담대교 남단", "6.5km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("11", "분당수서로 (탄천IC)", "7.2km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                CctvInfo("12", "서울외곽순환 (송파IC)", "8.0km", "https://www.utic.go.kr/img/cctv_sample.jpg", "")
            )
        )
    }
}
