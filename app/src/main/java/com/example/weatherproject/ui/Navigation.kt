package com.example.weatherproject.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(weatherState: WeatherState, navController: NavController, viewModel: MainViewModel) {

    val pageCount = 3 // 총 3페이지
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope() // 이동용

    Scaffold(
        topBar = { WeatherTopAppBar(viewModel = viewModel, navController = navController) },
        containerColor = Color.Transparent
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> {
                        // [1페이지]
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
                        ) {
                            // 현재 날씨 -> (클릭 시 상세 페이지(1)로)
                            CurrentWeatherCard(
                                weather = weatherState.currentWeather,
                                address = weatherState.currentAddress,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                                }
                            )

                            // ⭐️ [수정됨] 시간별 예보 -> (클릭 시 주간 예보 페이지(2)로 이동!)
                            HourlyForecastCard(
                                hourlyForecasts = weatherState.hourlyForecast,
                                onClick = {
                                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                                }
                            )

                            CctvCard(onClick = { navController.navigate("cctv") })
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    1 -> {
                        // [2페이지] 상세
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            WeatherDetailCard(details = weatherState.weatherDetails)
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                    2 -> {
                        // [3페이지] 주간
                        Column(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            WeeklyForecastCard(weeklyForecasts = weatherState.weeklyForecast)
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
            PagerIndicator(
                pagerState = pagerState,
                pageCount = pageCount,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopAppBar(
    viewModel: MainViewModel,
    navController: NavController
) {
    val searchText by viewModel.searchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
    ) {
        Column {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { viewModel.onSearchTextChange(it) },
                        label = { Text("도시 검색", color = Color.White.copy(alpha = 0.7f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
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
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "설정")
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
                                    viewModel.onCitySelected(city)
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        ),
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
fun HourlyForecastCard(
    hourlyForecasts: List<HourlyForecast>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.3f)
        )
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    WeatherProjectTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF60A5FA) // 연한 파란색
        ) {
            val previewNavController = rememberNavController()
            val previewViewModel: MainViewModel = viewModel()
            HomeScreen(WeatherState(), previewNavController, previewViewModel)
        }
    }
}