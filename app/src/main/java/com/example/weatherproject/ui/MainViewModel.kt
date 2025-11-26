// MainViewModel.kt (전체 코드 - 18시간 예보 + 검색 기능 포함)

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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val preferenceManager = PreferenceManager(application)

    // 메인 날씨 상태 (UI가 바라보는 데이터)
    private val _uiState = MutableStateFlow(WeatherState())
    val uiState: StateFlow<WeatherState> = _uiState

    // 검색 결과 리스트
    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // 사용자가 입력 중인 검색어
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

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

    // ⭐️ 검색어가 바뀔 때마다 호출되는 함수
    fun onSearchTextChange(text: String) {
        _searchText.value = text

        if (text.length > 0) {
            // 가짜 검색 결과 (나중에 API 연동 시 교체)
            _searchResults.value = listOf(
                "$text",
                "$text 시",
                "$text 구",
                "$text 동"
            )
        } else {
            _searchResults.value = emptyList()
        }
    }

    // ⭐️ 도시를 클릭했을 때 호출되는 함수
    fun onCitySelected(context: Context, city: String) {
        _searchText.value = "" // 검색창 비우기
        _searchResults.value = emptyList() // 리스트 숨기기

        // (가짜) 선택한 도시로 주소 업데이트
        val currentState = _uiState.value
        _uiState.value = currentState.copy(currentAddress = city)

        viewModelScope.launch(Dispatchers.IO) {
            val coordinates = getCoordinatesFromCityName(context, city)
            if (coordinates != null) {
                Log.d("MainViewModel", "Selected City: $city, Lat: ${coordinates.first}, Lon: ${coordinates.second}")
                // TODO: 여기서 위도(coordinates.first), 경도(coordinates.second)를 사용하여 날씨 API 호출
                // searchWeatherByCoordinates(coordinates.first, coordinates.second)
            } else {
                Log.e("MainViewModel", "Failed to get coordinates for $city")
            }
        }
    }

    // Geocoder를 사용하여 주소를 위도, 경도로 변환하는 함수
    private fun getCoordinatesFromCityName(context: Context, cityName: String): Pair<Double, Double>? {
        return try {
            // Geocoder가 존재하는지 확인 (일부 기기 미지원 가능성)
            if (!Geocoder.isPresent()) return null

            val geocoder = Geocoder(context, Locale.KOREA)
            // 안드로이드 13(API 33) 이상부터는 리스너 방식이 권장되지만, 하위 호환성을 위해 동기 방식 사용 (Dispatchers.IO에서 호출)
            val addresses = geocoder.getFromLocationName(cityName, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                Pair(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 가짜 데이터 로드 (UI 테스트용)
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

            // ⭐️ 시간별 예보 (18시간으로 늘림)
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
                // ⭐️ 추가된 6시간 (새벽~아침)
                HourlyForecast("02:00", iconPartlyCloudy, "10°", "0mm", "좋음"),
                HourlyForecast("03:00", iconSunny, "9°", "0mm", "좋음"),
                HourlyForecast("04:00", iconSunny, "8°", "0mm", "좋음"),
                HourlyForecast("05:00", iconSunny, "8°", "0mm", "좋음"),
                HourlyForecast("06:00", iconPartlyCloudy, "9°", "0mm", "보통"),
                HourlyForecast("07:00", iconSunny, "11°", "0mm", "보통")
            ),

            // 주간 예보 (7일)
            weeklyForecast = listOf(
                WeeklyForecast(date = "11/17 (월)", iconSunny, "미세먼지 보통", "0mm", "15°", "23°"),
                WeeklyForecast(date = "11/18 (화)", iconPartlyCloudy, "미세먼지 나쁨", "0mm", "16°", "24°"),
                WeeklyForecast(date = "11/19 (수)", iconCloudy, "미세먼지 나쁨", "0mm", "14°", "22°"),
                WeeklyForecast(date = "11/20 (목)", iconRain, "미세먼지 좋음", "25mm", "13°", "20°"),
                WeeklyForecast(date = "11/21 (금)", iconRain, "미세먼지 좋음", "40mm", "12°", "19°"),
                WeeklyForecast(date = "11/22 (토)", iconSunny, "미세먼지 보통", "0mm", "13°", "22°"),
                WeeklyForecast(date = "11/23 (일)", iconPartlyCloudy, "미세먼지 보통", "0mm", "15°", "24°")
            )
        )
    }

    // 위치 담당자용 껍데기 함수
    fun refreshMyLocation() {
        // TODO: GPS 로직 구현 필요
        val currentState = _uiState.value
        _uiState.value = currentState.copy(currentAddress = "서울시 강남구 (갱신됨)")
    }

    // API 담당자용 껍데기 함수
    fun searchWeatherByCity(city: String) {
        // TODO: API 로직 구현 필요
    }
}