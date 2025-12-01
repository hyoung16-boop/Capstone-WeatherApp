package com.example.weatherproject.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.ui.MainViewModel
import com.example.weatherproject.ui.SearchViewModel
import com.example.weatherproject.ui.components.NearbyCctvCard
import com.example.weatherproject.ui.components.ClothingRecommendationCard
import com.example.weatherproject.ui.components.CurrentWeatherCard
import com.example.weatherproject.ui.components.HourlyForecastCard
import com.example.weatherproject.ui.components.WeatherTopAppBar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    weatherState: WeatherState,
    navController: NavController,
    viewModel: MainViewModel,
    searchViewModel: SearchViewModel
) {
    val showDialog by viewModel.showSetupDialog.collectAsState()
    val tempAdjustment by viewModel.tempAdjustment.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshData() }
    )

    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(true) {
        viewModel.errorEvent.collect { message ->
            scaffoldState.snackbarHostState.showSnackbar(message)
        }
    }

    var isDetailExpanded by remember { mutableStateOf(false) }
    var isForecastExpanded by remember { mutableStateOf(false) }

    if (showDialog) {
        TemperaturePreferenceDialog(onDismiss = { }) { adjustment ->
            viewModel.saveTempAdjustment(adjustment)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            WeatherTopAppBar(
                viewModel = viewModel,
                searchViewModel = searchViewModel,
                navController = navController
            )
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AnimatedVisibility(
                    visible = isRefreshing,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "최신 날씨 정보를 불러오는 중입니다...",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                // ⭐️ address 사용
                CurrentWeatherCard(
                    weather = weatherState.currentWeather,
                    address = weatherState.address,
                    details = weatherState.weatherDetails,
                    isExpanded = isDetailExpanded,
                    onClick = { isDetailExpanded = !isDetailExpanded }
                )

                HourlyForecastCard(
                    hourlyForecasts = weatherState.hourlyForecast,
                    weeklyForecasts = weatherState.weeklyForecast,
                    isExpanded = isForecastExpanded,
                    onClick = { isForecastExpanded = !isForecastExpanded }
                )

                ClothingRecommendationCard(
                    currentTemp = weatherState.currentWeather.temperature,
                    feelsLike = weatherState.currentWeather.feelsLike,
                    tempAdjustment = tempAdjustment
                )

                // ⭐️ 빈 리스트로 대체
                NearbyCctvCard(
                    cctvList = emptyList(),
                    onMoreClick = { navController.navigate("cctv") },
                    onCctvClick = { cctv -> }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = Color(0xFF2563EB),
                scale = true
            )
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("추위 많이", fontSize = 12.sp, color = Color.Blue)
                    Text("보통", fontSize = 12.sp, color = Color.Black)
                    Text("더위 많이", fontSize = 12.sp, color = Color.Red)
                }

                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = -3f..3f,
                    steps = 5,
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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Preview
}