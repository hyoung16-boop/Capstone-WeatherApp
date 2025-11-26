// MainViewModel.kt (전체 코드 - 18시간 예보 + 검색 기능 포함)

package com.example.weatherproject.ui

import androidx.lifecycle.ViewModel
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    // 메인 날씨 상태 (UI가 바라보는 데이터)
    private val _uiState = MutableStateFlow(WeatherState())
    val uiState: StateFlow<WeatherState> = _uiState

    // ⭐️ 검색 결과 리스트
    private val _searchResults = MutableStateFlow<List<String>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // ⭐️ 사용자가 입력 중인 검색어
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    init {
        loadFakeData()
    }

    // ⭐️ 검색어가 바뀔 때마다 호출되는 함수
    fun onSearchTextChange(text: String) {
        _searchText.value = text

        if (text.length > 0) {
            // 가짜 검색 결과 (나중에 API 연동 시 교체)
            _searchResults.value = listOf(
                "$text 시청",
                "$text 강남구",
                "$text 해운대구",
                "$text 역"
            )
        } else {
            _searchResults.value = emptyList()
        }
    }

    // ⭐️ 도시를 클릭했을 때 호출되는 함수
    fun onCitySelected(city: String) {
        _searchText.value = "" // 검색창 비우기
        _searchResults.value = emptyList() // 리스트 숨기기

        // (가짜) 선택한 도시로 주소 업데이트
        val currentState = _uiState.value
        _uiState.value = currentState.copy(currentAddress = city)

        // TODO: 여기서 API 담당자가 만든 '진짜 날씨 가져오는 함수'를 호출해야 함
        // searchWeatherByCity(city)
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