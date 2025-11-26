package com.example.weatherproject.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.weatherproject.R
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.ui.theme.WeatherProjectTheme
import kotlinx.coroutines.launch

@Composable
fun WeatherNavHost(weatherState: WeatherState, viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                weatherState = weatherState,
                navController = navController,
                viewModel = viewModel
            )
        }
        composable("detail") {
            DetailWeatherScreen(
                weatherState = weatherState,
                navController = navController
            )
        }
        composable("forecast") {
            ForecastScreen(
                weatherState = weatherState,
                navController = navController
            )
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("cctv") {
            CctvScreen(navController = navController)
        }
        composable("alarm_list") {
            AlarmListScreen(navController = navController)
        }
        composable(
            route = "alarm_edit?alarmId={alarmId}",
            arguments = listOf(navArgument("alarmId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getInt("alarmId") ?: -1
            AlarmEditScreen(navController = navController, alarmId = alarmId)
        }
    }
}

@Composable
fun HomeScreen(weatherState: WeatherState, navController: NavController, viewModel: MainViewModel) {
    // 최초 설문 다이얼로그 표시 여부
    val showDialog by viewModel.showSetupDialog.collectAsState()
    // 저장된 체질 보정값
    val tempAdjustment by viewModel.tempAdjustment.collectAsState()

    if (showDialog) {
        TemperaturePreferenceDialog(onDismiss = { /* 강제 설정 유도 (취소 불가) */ }) { adjustment ->
            viewModel.saveTempAdjustment(adjustment)
        }
    }

    Scaffold(
        topBar = { WeatherTopAppBar(viewModel = viewModel, navController = navController) },
        backgroundColor = Color.Transparent
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 현재 날씨
            CurrentWeatherCard(
                weather = weatherState.currentWeather,
                address = weatherState.currentAddress,
                onClick = { navController.navigate("detail") }
            )

            // 시간별 예보
            HourlyForecastCard(
                hourlyForecasts = weatherState.hourlyForecast,
                onClick = { navController.navigate("forecast") }
            )

            // 옷차림 추천 (보정값 전달)
            ClothingRecommendationCard(
                feelsLike = weatherState.currentWeather.feelsLike,
                tempAdjustment = tempAdjustment
            )

            // CCTV
            CctvCard(onClick = { navController.navigate("cctv") })
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TemperaturePreferenceDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var sliderPosition by remember { mutableStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "체질 맞춤 설정",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "더위나 추위를 많이 타시나요?\n옷차림 추천에 반영해 드립니다.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 슬라이더 라벨
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("추위 많이", fontSize = 12.sp, color = Color.Blue)
                    Text("보통", fontSize = 12.sp, color = Color.Black)
                    Text("더위 많이", fontSize = 12.sp, color = Color.Red)
                }

                // 슬라이더 (-3 ~ +3)
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = -3f..3f,
                    steps = 5, // -3, -2, -1, 0, 1, 2, 3 (총 7개 값, steps는 중간 단계 개수이므로 5)
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colors.primary,
                        activeTrackColor = MaterialTheme.colors.primary
                    )
                )
                
                Text(
                    text = "보정값: ${sliderPosition.toInt()}",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onConfirm(sliderPosition.toInt()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("설정 완료")
                }
            }
        }
    }
}

@Composable
fun DetailWeatherScreen(weatherState: WeatherState, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상세 날씨 정보", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // ArrowForward를 180도 회전하여 뒤로가기 아이콘으로 사용
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "뒤로 가기",
                            tint = Color.White,
                            modifier = Modifier.rotate(180f)
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            CurrentWeatherCard(
                weather = weatherState.currentWeather,
                address = weatherState.currentAddress,
                onClick = { } // 상세 화면에서는 클릭 동작 없음
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            WeatherDetailCard(details = weatherState.weatherDetails)
        }
    }
}

@Composable
fun ForecastScreen(weatherState: WeatherState, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("예보 정보", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward, 
                            contentDescription = "뒤로 가기", 
                            tint = Color.White,
                            modifier = Modifier.rotate(180f) // ArrowBack 대신 ArrowForward를 180도 회전해서 사용 (기존 리소스 활용)
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // 시간별 예보
            HourlyForecastCard(
                hourlyForecasts = weatherState.hourlyForecast,
                onClick = { } // 예보 화면에서는 클릭 동작 없음
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 주간 예보
            WeeklyForecastCard(weeklyForecasts = weatherState.weeklyForecast)
        }
    }
}

@Composable
fun WeatherTopAppBar(
    viewModel: MainViewModel,
    navController: NavController
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        Column {
            TopAppBar(
                backgroundColor = Color.Transparent,
                contentColor = Color.White,
                elevation = 0.dp,
                title = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { viewModel.onSearchTextChange(it) },
                        label = { Text("도시 검색", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshMyLocation() }) {
                        Icon(Icons.Default.LocationOn, "현재 위치")
                    }
                    IconButton(onClick = { navController.navigate("alarm_list") }) {
                        Icon(Icons.Default.Notifications, "알림 설정")
                    }
                }
            )

            if (searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    items(searchResults) { city ->
                        Text(
                            text = city,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onCitySelected(context, city)
                                }
                                .padding(16.dp),
                            color = Color.Black
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(
    weather: CurrentWeather,
    address: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = address,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = weather.iconUrl,
                    contentDescription = weather.description,
                    modifier = Modifier.size(80.dp),
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    Text(
                        text = weather.temperature,
                        fontSize = 48.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weather.description,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${weather.maxTemp} / ${weather.minTemp}",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    // 체감 온도 표시 추가
                    Text(
                        text = weather.feelsLike,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailCard(details: WeatherDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "날씨 상세",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            DetailRowItem(icon = "☔", label = "강수량", value = details.precipitation)
            Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

            DetailRowItem(icon = "💧", label = "습도", value = details.humidity)
            Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

            DetailRowItem(icon = "🌬️", label = "풍속", value = details.wind)
            Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

            DetailRowItem(icon = "😷", label = "미세먼지", value = details.pm10)
        }
    }
}

@Composable
fun DetailRowItem(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun CctvCard(onClick: () -> Unit) {
    Card(
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = "CCTV",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "실시간 CCTV",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "교통 상황 확인하기",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "이동",
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun ClothingRecommendationCard(feelsLike: String, tempAdjustment: Int) {
    // 체감 온도 숫자만 추출 (예: "15°C" -> 15)
    val rawTemp = feelsLike.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 20
    
    // 보정값 적용 (더위 많이 탐: +값 -> 체감 온도를 더 높게 인식 -> 더 시원한 옷 추천?)
    // 아니, 로직 수정:
    // 더위를 많이 탐(+3) -> 20도일 때 23도 기준으로 옷을 입어야 함 (시원하게) -> OK
    // 추위를 많이 탐(-3) -> 20도일 때 17도 기준으로 옷을 입어야 함 (따뜻하게) -> OK
    val adjustedTemp = rawTemp + tempAdjustment

    val recommendationText = when {
        adjustedTemp >= 28 -> "민소매, 반팔, 반바지, 원피스"
        adjustedTemp >= 23 -> "반팔, 얇은 셔츠, 반바지, 면바지"
        adjustedTemp >= 20 -> "얇은 가디건, 긴팔, 면바지, 청바지"
        adjustedTemp >= 17 -> "얇은 니트, 맨투맨, 가디건, 청바지"
        adjustedTemp >= 12 -> "자켓, 가디건, 야상, 스타킹, 청바지, 면바지"
        adjustedTemp >= 9 -> "자켓, 트렌치코트, 야상, 니트, 청바지, 스타킹"
        adjustedTemp >= 5 -> "코트, 가죽자켓, 히트텍, 니트, 레깅스"
        else -> "패딩, 두꺼운 코트, 목도리, 기모제품"
    }

    val adjustmentText = if (tempAdjustment > 0) "(더위 많이 탐)" else if (tempAdjustment < 0) "(추위 많이 탐)" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Face, // 임시 아이콘
                    contentDescription = "옷차림 추천",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "오늘의 옷차림 추천 $adjustmentText",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recommendationText,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "체감 온도: $feelsLike (보정: ${if(tempAdjustment > 0) "+" else ""}$tempAdjustment)",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun HourlyForecastCard(
    hourlyForecasts: List<HourlyForecast>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "시간별 예보",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "더보기",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(hourlyForecasts) { forecast ->
                    HourlyForecastItem(forecast = forecast)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(forecast: HourlyForecast) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = forecast.time, fontSize = 14.sp, color = Color.White)

        AsyncImage(
            model = forecast.iconUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        Text(text = forecast.temperature, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WeeklyForecastCard(weeklyForecasts: List<WeeklyForecast>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "주간 예보",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weeklyForecasts.forEach { forecast ->
                    WeeklyForecastItem(forecast = forecast)
                    if (forecast != weeklyForecasts.last()) {
                        Divider(color = Color.White.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyForecastItem(forecast: WeeklyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = forecast.date,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(2.3f)
        )
        Text(
            text = forecast.pm10Status,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.weight(2.5f)
        )
        Text(
            text = forecast.precipitation,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = "${forecast.minTemp} / ${forecast.maxTemp}",
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Preview에서는 Application Context를 사용할 수 없으므로 가짜 ViewModel 또는 빈 상태 사용 필요
    // 여기서는 Preview 코드 유지
}