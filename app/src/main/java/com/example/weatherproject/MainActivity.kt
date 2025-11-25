// MainActivity.kt (10단계: '주간 예보' 한 줄 버그까지 수정한 최종본)

package com.example.weatherproject

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background // ⬅️ '점'을 그리기 위해 필요
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState // ⬅️ '점'을 그리기 위해 필요
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape // ⬅️ '점'을 그리기 위해 필요
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // ⬅️ '점'을 그리기 위해 필요
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.ui.MainViewModel
import com.example.weatherproject.ui.WeatherNavHost
import com.example.weatherproject.ui.theme.WeatherProjectTheme


class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            val requestPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Show the notification.
                    showFullScreenNotification(context, viewModel.uiState.value)
                } else {
                    // Handle the case where the user denies the permission.
                }
            }

            LaunchedEffect(key1 = true) {
                // Request notification permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    )) {
                        PackageManager.PERMISSION_GRANTED -> {
                            // Permission already granted
                            showFullScreenNotification(context, viewModel.uiState.value)
                        }
                        else -> {
                            // Request permission
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    // For older versions (Android 11, 12), permission is granted by default
                    showFullScreenNotification(context, viewModel.uiState.value)
                }
            }

            WeatherProjectTheme {
                val weatherState by viewModel.uiState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF60A5FA) // 연한 파란색
                ) {
                    WeatherNavHost(weatherState)
                }
            }
        }
    }
}

// 2. 앱 화면의 전체 뼈대 (9단계 '표시기' 적용됨)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WeatherApp(weatherState: WeatherState, onSettingsClick: () -> Unit) {

    val pageCount = 2 // 총 2페이지
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Scaffold(
        topBar = {
            WeatherTopAppBar(onSettingsClick = onSettingsClick)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. '가로로 넘기는' 페이지 뼈대 (배경)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                if (page == 0) {
                    // 1페이지: '오늘' (세로 스크롤)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CurrentWeatherCard(weather = weatherState.currentWeather)
                        HourlyForecastCard(hourlyForecasts = weatherState.hourlyForecast)
                        CctvCard(onClick = { /* CCTV 페이지로 이동 */ })
                        Spacer(modifier = Modifier.height(32.dp)) // '표시기'에 가려지지 않게 바닥 여백
                    }
                } else {
                    // 2페이지: '주간/상세' (세로 스크롤)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        WeatherDetailCard(details = weatherState.weatherDetails)
                        WeeklyForecastCard(weeklyForecasts = weatherState.weeklyForecast)
                        Spacer(modifier = Modifier.height(32.dp)) // '표시기'에 가려지지 않게 바닥 여백
                    }
                }
            }

            // 2. '페이지 표시기' (점 2개)
            PagerIndicator(
                pagerState = pagerState,
                pageCount = pageCount,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

// 3. '상단 검색창' (TopAppBar)을 그리는 함수
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopAppBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        title = {
            OutlinedTextField(
                value = "",
                onValueChange = { },
                label = { Text("도시 검색...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            )
        },
        actions = {
            IconButton(onClick = { /* '내 위치' 기능 */ }) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "현재 위치"
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "설정"
                )
            }
        }
    )
}

// 4. '현재 날씨' 카드를 그리는 함수
@Composable
fun CurrentWeatherCard(weather: CurrentWeather) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = weather.iconUrl,
                contentDescription = weather.description,
                modifier = Modifier.size(80.dp),
                placeholder = painterResource(id = android.R.drawable.stat_sys_warning)
            )
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = "%.0f°".format(weather.temperature), // Format Double to String
                    fontSize = 48.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                // Display the calculated feels-like temperature
                Text(
                    text = "체감: %.1f°".format(weather.feelsLike),
                    fontSize = 16.sp,
                    color = Color.White
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
            }
        }
    }
}

// 5. '날씨 상세' 카드를 그리는 함수 ('잘리는 버그' 수정한 최종본)
@Composable
fun WeatherDetailCard(details: WeatherDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "날씨 상세",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Format Double values to String before passing to WeatherDetailItem
            Row(Modifier.fillMaxWidth()) {
                WeatherDetailItem(label = "체감 온도", value = "%.1f°".format(details.feelsLike), modifier = Modifier.weight(1f))
                WeatherDetailItem(label = "습도", value = "%.0f%%".format(details.humidity), modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth()) {
                WeatherDetailItem(label = "풍속", value = "%.1f km/h".format(details.wind), modifier = Modifier.weight(1f))
                WeatherDetailItem(label = "기압", value = details.pressure, modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth()) {
                WeatherDetailItem(label = "가시거리", value = details.visibility, modifier = Modifier.weight(1f))
                WeatherDetailItem(label = "자외선 지수", value = details.uvIndex, modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth()) {
                WeatherDetailItem(label = "강수량", value = details.precipitation, modifier = Modifier.weight(1f))
                WeatherDetailItem(label = "미세먼지", value = details.pm10, modifier = Modifier.weight(1f))
            }
        }
    }
}

// '날씨 상세' 카드 안에 들어갈 '작은 조각'
@Composable
fun WeatherDetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
        Text(text = value, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// 6. 'CCTV' 카드를 그리는 함수
@Composable
fun CctvCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
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

// 7. '시간별 예보' 카드를 그리는 함수
@Composable
fun HourlyForecastCard(hourlyForecasts: List<HourlyForecast>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "시간별 예보",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(hourlyForecasts) { forecast ->
                    HourlyForecastItem(forecast = forecast)
                }
            }
        }
    }
}

// '시간별 예보' 카드 안에 들어갈 '작은 조각'
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
            placeholder = painterResource(id = android.R.drawable.stat_sys_warning)
        )

        Text(text = forecast.temperature, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

// 8. '주간 예보' 카드를 그리는 함수
@Composable
fun WeeklyForecastCard(weeklyForecasts: List<WeeklyForecast>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
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

// MainActivity.kt 파일에서, 'WeeklyForecastItem' 함수 하나만 이걸로 '교체'하세요.

// ⭐️ 10단계 '최종 수정': '주간 예보'의 '한 줄' (비율 수정한 최종본)
@Composable
fun WeeklyForecastItem(forecast: WeeklyForecast) {
    // '가로'로 4개 항목을 배치
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // 높이
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 날짜
        Text(
            text = forecast.date,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(2.3f) // ⬅️ 비율 수정 (2.5 -> 2.3)
        )

        // 2. 미세먼지
        Text(
            text = forecast.pm10Status,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.weight(2.5f) // ⬅️ 비율 수정 (2.2 -> 2.5) (왼쪽으로 당겨짐)
        )

        // 3. 강수량
        Text(
            text = forecast.precipitation,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.weight(1.2f) // ⬅️ 비율 수정 (1.3 -> 1.2)
        )

        // 4. 최저/최고 온도
        Text(
            text = "${forecast.minTemp} / ${forecast.maxTemp}",
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f) // (비율 유지)
        )
    }
}

// 9. '페이지 표시기' (점)를 그리는 함수
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerIndicator(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.5f)
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}


// 10. 미리보기
@Preview(showBackground = true)
@Composable
fun WeatherAppPreview() {
    WeatherProjectTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF60A5FA) // 연한 파란색
        ) {
            WeatherApp(WeatherState(), onSettingsClick = {})
        }
    }
}

// This function creates and shows the full-screen notification
fun showFullScreenNotification(context: Context, weatherState: WeatherState) {
    // Prepare notification content
    val weather = weatherState.currentWeather
    val notificationContent = "기온: %.0f° (체감: %.0f°) | 상태: %s".format(
        weather.temperature,
        weather.feelsLike,
        weather.description
    )
    
    // Use the helper class
    com.example.weatherproject.util.NotificationHelper.showNotification(context, "현재 날씨", notificationContent)
}