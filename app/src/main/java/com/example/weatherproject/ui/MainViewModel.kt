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
import kotlinx.coroutines.flow.asSharedFlow
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

    // 에러 메시지 (일회성 이벤트)
    private val _errorEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

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
            if (_isRefreshing.value) return@launch // 이미 진행 중이면 무시
            _isRefreshing.value = true
            
            try {
                // 1. 위치 갱신 시늉 (실제로는 GPS 요청)
                val currentState = _uiState.value
                // 로딩 중임을 알리기 위해 주소만 살짝 변경하거나, UI에서 isRefreshing으로 처리
                
                kotlinx.coroutines.delay(2000) // 2초 딜레이 (로딩 시뮬레이션)

                // 2. 랜덤 에러 발생 테스트 (20% 확률)
                if ((1..5).random() == 1) {
                    throw Exception("네트워크 연결이 불안정합니다. 다시 시도해주세요.")
                }
                
                _uiState.value = currentState.copy(currentAddress = "서울, 대한민국 (갱신됨)")

                // 3. 날씨 API 호출 시늉 (실제로는 서버 요청)
                kotlinx.coroutines.delay(500) 
                
                // 랜덤 날씨 생성 (배경색 테스트용)
                val weatherTypes = listOf(
                    Triple("https://openweathermap.org/img/wn/01d@2x.png", "Clear Sky", "맑음"),
                    Triple("https://openweathermap.org/img/wn/02d@2x.png", "Partly Cloudy", "구름 조금"),
                    Triple("https://openweathermap.org/img/wn/03d@2x.png", "Cloudy", "흐림"),
                    Triple("https://openweathermap.org/img/wn/10d@2x.png", "Rain", "비")
                )
                val randomWeather = weatherTypes.random()
                
                // 현재 온도 랜덤 변화
                val newTempInt = (10..30).random()
                val newTempStr = "${newTempInt}°"

                val current = _uiState.value.currentWeather
                val details = _uiState.value.weatherDetails

                // 체감 온도 재계산 (값과 이유를 함께 받아옴)
                val windSpeed = details.wind.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
                val humidity = details.humidity.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
                
                val (newFeelsLikeInt, reason) = calculateFeelsLike(newTempInt.toDouble(), windSpeed, humidity)
                // 이유가 있으면 괄호로 감싸고, 없으면 그냥 둠
                val reasonText = if (reason.isNotEmpty()) "($reason)" else ""
                val newFeelsLikeStr = "${newFeelsLikeInt}° $reasonText"
                
                // 1. 현재 날씨 상태 업데이트
                val updatedCurrentWeather = current.copy(
                    iconUrl = randomWeather.first, 
                    description = randomWeather.third, 
                    temperature = newTempStr,
                    feelsLike = "체감: $newFeelsLikeStr" 
                )

                // 2. 시간별 예보의 첫 번째 항목("지금")도 현재 날씨와 동기화
                val currentHourlyList = _uiState.value.hourlyForecast
                val updatedHourlyList = currentHourlyList.mapIndexed { index, item ->
                    if (index == 0) {
                        // "지금" 항목을 현재 날씨 데이터로 덮어쓰기
                        item.copy(
                            iconUrl = randomWeather.first,
                            temperature = newTempStr,
                            precipitation = if (randomWeather.second == "Rain") "5mm" else "0mm",
                            pm10Status = if (randomWeather.second == "Rain") "좋음" else "보통"
                        )
                    } else {
                        item // 나머지는 그대로 유지
                    }
                }

                // 상태 업데이트 (CurrentWeather와 HourlyForecast 모두 갱신)
                _uiState.value = _uiState.value.copy(
                    currentWeather = updatedCurrentWeather,
                    weatherDetails = details.copy(
                        feelsLike = newFeelsLikeInt.toString() + "°"
                    ),
                    hourlyForecast = updatedHourlyList
                )
            } catch (e: Exception) {
                _errorEvent.emit(e.message ?: "알 수 없는 오류가 발생했습니다.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    // 체감 온도 계산 로직 (표준 공식 적용)
    private fun calculateFeelsLike(temp: Double, wind: Double, humidity: Double): Pair<Int, String> {
        // 1. 겨울철 (10도 이하, 풍속 4.8km/h 이상) - JAG/TI 공식 (Wind Chill)
        if (temp <= 10.0 && wind >= 4.8) {
            val vPow = Math.pow(wind, 0.16)
            val result = (13.12 + 0.6215 * temp - 11.37 * vPow + 0.3965 * temp * vPow).toInt()
            return Pair(result, "바람이 불어 더 춥게 느껴져요")
        }
        
        // 2. 여름철 (25도 이상) - 습도 영향 (Heat Index 약식 적용)
        if (temp >= 25.0) {
             // 섭씨 -> 화씨 변환
            val tf = temp * 9.0 / 5.0 + 32.0
            val rh = humidity
            
            // Heat Index Formula (Rothfusz Regression)
            val hiF = -42.379 + 2.04901523 * tf + 10.14333127 * rh - 0.22475541 * tf * rh - 
                      0.00683783 * tf * tf - 0.05481717 * rh * rh + 0.00122874 * tf * tf * rh + 
                      0.00085282 * tf * rh * rh - 0.00000199 * tf * tf * rh * rh
            
            // 화씨 -> 섭씨 복귀
            val hiC = (hiF - 32.0) * 5.0 / 9.0
            
            val result = hiC.toInt()
            val reason = if (result > temp) "습도가 높아 더 덥게 느껴져요" else ""
            return Pair(result, reason)
        }

        // 그 외 (봄/가을)
        return Pair(temp.toInt(), "")
    }

    fun refreshMyLocation() {
        refreshData()
    }

    // 내 위치 기반 CCTV 검색 시뮬레이션
    fun fetchCurrentLocationCctvs() {
        viewModelScope.launch {
            if (_isRefreshing.value) return@launch
            _isRefreshing.value = true
            
            try {
                // 1. 위치 탐색 시늉
                val currentState = _uiState.value
                // 실제로는 여기서 GPS로 lat, lon을 가져옴
                
                kotlinx.coroutines.delay(1500) // 위치 찾는 딜레이
                
                // 2. 내 위치 주변 CCTV 데이터 (가상 데이터 교체)
                // 기존 데이터와 다르게 "내 위치" 느낌이 나는 데이터로 구성
                val myLocationCctvs = listOf(
                    CctvInfo("99", "내 위치 (집 앞)", "0.1km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                    CctvInfo("98", "동네 사거리", "0.3km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                    CctvInfo("97", "지하철역 입구", "0.7km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                    CctvInfo("96", "대로변 (버스정류장)", "1.2km", "https://www.utic.go.kr/img/cctv_sample.jpg", ""),
                    CctvInfo("95", "구청 앞 교차로", "1.5km", "https://www.utic.go.kr/img/cctv_sample.jpg", "")
                )

                _uiState.value = currentState.copy(
                    currentAddress = "내 위치 (자동 갱신됨)",
                    cctvList = myLocationCctvs
                )
                
            } catch (e: Exception) {
                _errorEvent.emit("위치 정보를 가져오는데 실패했습니다.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updateWeatherByLocation(city: String, lat: Double, lon: Double) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(currentAddress = city)
        Log.d("MainViewModel", "Weather update requested for: $city ($lat, $lon)")
        // TODO: API Call
    }

    private fun loadFakeData() {
        // OpenWeather 공식 아이콘 주소 (HTTPS 적용)
        val iconSunny = "https://openweathermap.org/img/wn/01d@2x.png"
        val iconPartlyCloudy = "https://openweathermap.org/img/wn/02d@2x.png"
        val iconCloudy = "https://openweathermap.org/img/wn/03d@2x.png"
        val iconRain = "https://openweathermap.org/img/wn/10d@2x.png"

        // 현재 시간 기준 시간별 예보 생성
        val currentCal = java.util.Calendar.getInstance()
        val hourlyList = mutableListOf<HourlyForecast>()
        val hourFormat = java.text.SimpleDateFormat("HH:00", Locale.getDefault())

        // "지금" 추가 (현재 날씨 상태 기반)
        hourlyList.add(HourlyForecast("지금", iconPartlyCloudy, "18°", "0mm", "보통"))

        // 이후 23시간 추가 (랜덤 데이터)
        for (i in 1..23) {
            currentCal.add(java.util.Calendar.HOUR_OF_DAY, 1)
            val timeStr = hourFormat.format(currentCal.time)
            
            // 밤/낮에 따라 아이콘 대충 변화 주기 (오전 6시~오후 6시: 해, 그 외: 달/구름)
            val hour = currentCal.get(java.util.Calendar.HOUR_OF_DAY)
            val icon = if (hour in 6..18) iconSunny else iconCloudy
            val temp = "${(10..22).random()}°" // 온도는 10~22도 사이 랜덤
            
            // 강수량도 가끔 있게
            val rain = if ((0..5).random() == 0) "5mm" else "0mm"
            val pm = if (rain != "0mm") "좋음" else "보통"
            
            hourlyList.add(HourlyForecast(timeStr, icon, temp, rain, pm))
        }

        // 현재 날짜 기준 주간 예보 생성 (랜덤 데이터)
        val weeklyCal = java.util.Calendar.getInstance()
        val weeklyList = mutableListOf<WeeklyForecast>()
        val dateFormat = java.text.SimpleDateFormat("MM/dd (E)", Locale.getDefault()) 

        for (i in 0..6) {
            val dateStr = dateFormat.format(weeklyCal.time)
            // 3일에 한번 비, 2일에 한번 흐림 등 랜덤 패턴
            val icon = if (i % 3 == 0) iconRain else if (i % 2 == 0) iconCloudy else iconSunny
            
            weeklyList.add(
                WeeklyForecast(
                    date = dateStr,
                    iconUrl = icon,
                    pm10Status = "미세먼지 ${listOf("좋음", "보통", "나쁨").random()}",
                    precipitation = "${(0..40).random()}mm",
                    minTemp = "${(5..15).random()}°",
                    maxTemp = "${(18..25).random()}°"
                )
            )
            weeklyCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        _uiState.value = WeatherState(
            currentAddress = "서울, 대한민국",

            currentWeather = CurrentWeather(
                iconUrl = iconPartlyCloudy,
                temperature = "18°",
                description = "구름 조금",
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

            hourlyForecast = hourlyList,

            weeklyForecast = weeklyList,
            
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
