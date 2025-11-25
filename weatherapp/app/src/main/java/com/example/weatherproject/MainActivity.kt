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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState // ⬅️ '점'을 그리기 위해 필요
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape // ⬅️ '점'을 그리기 위해 필요
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import com.example.weatherproject.data.*
import com.example.weatherproject.ui.MainViewModel
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
                    showFullScreenNotification(context, viewModel.uiState.value.recommendation)
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
                            showFullScreenNotification(context, viewModel.uiState.value.recommendation)
                        }
                        else -> {
                            // Request permission
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    // For older versions (Android 11, 12), permission is granted by default
                    showFullScreenNotification(context, viewModel.uiState.value.recommendation)
                }
            }

            WeatherProjectTheme {
                val weatherState by viewModel.uiState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF60A5FA) // 연한 파란색
                ) {
                    WeatherApp(weatherState)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WeatherApp(weatherState: WeatherState) {

    val pageCount = 2 // 총 2페이지
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Scaffold(
        topBar = {
            WeatherTopAppBar()
        },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                if (page == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CurrentWeatherCard(weather = weatherState.currentWeather)
                        HourlyForecastCard(hourlyForecasts = weatherState.hourlyForecast)
                        weatherState.recommendation?.let {
                            RecommendationCard(recommendation = it)
                        }
                        CctvCard(onClick = { /* CCTV 페이지로 이동 */ })
                        Spacer(modifier = Modifier.height(32.dp)) // '표시기'에 가려지지 않게 바닥 여백
                    }
                } else {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopAppBar() {
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
            IconButton(onClick = { /* '설정' 페이지로 이동 */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "설정"
                )
            }
        }
    )
}

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

@Composable
fun WeatherDetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f))
        Text(text = value, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

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

@Composable
fun WeeklyForecastItem(forecast: WeeklyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // 높이
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

@Composable
fun RecommendationCard(recommendation: Recommendation) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "오늘의 추천",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Clothing Recommendation
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Checkroom,
                    contentDescription = "옷차림 추천",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "옷차림", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(text = recommendation.clothing, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.White.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            // Lifestyle Recommendation
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = "생활/행동 추천",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "생활/행동", fontSize = 16.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(text = recommendation.life, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherAppPreview() {
    WeatherProjectTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF60A5FA)
        ) {
            WeatherApp(WeatherState(
                recommendation = Recommendation("반팔, 반바지", "야외 활동 좋아요")
            ))
        }
    }
}

fun showFullScreenNotification(context: Context, recommendation: Recommendation?) {
    val channelId = "weather_alert_channel"
    val channelName = "Weather Alerts"
    val notificationId = 1

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for important weather alerts"
        }
        notificationManager.createNotificationChannel(channel)
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }

    val fullScreenPendingIntent = PendingIntent.getActivity(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    
    val notificationText = recommendation?.let {
        "옷차림: ${it.clothing} | 활동: ${it.life}"
    } ?: "오늘의 날씨를 확인하세요."

    val builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("오늘의 날씨 추천")
        .setContentText(notificationText)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setFullScreenIntent(fullScreenPendingIntent, true)
        .setAutoCancel(true)

    notificationManager.notify(notificationId, builder.build())
}