package com.example.weatherproject.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import com.example.weatherproject.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.ui.CctvViewModel
import com.example.weatherproject.ui.MainViewModel
import com.example.weatherproject.ui.SearchViewModel
import com.example.weatherproject.ui.components.ClothingRecommendationCard
import com.example.weatherproject.ui.components.CurrentWeatherCard
import com.example.weatherproject.ui.components.HourlyForecastCard
import com.example.weatherproject.ui.components.NearbyCctvCard
import com.example.weatherproject.ui.components.WeatherTopAppBar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    weatherState: WeatherState,
    navController: NavController,
    mainViewModel: MainViewModel,
    searchViewModel: SearchViewModel,
    cctvViewModel: CctvViewModel
) {
    val showSetupDialog by mainViewModel.showSetupDialog.collectAsState()
    val showTempAdjustmentDialog by mainViewModel.showTempAdjustmentDialog.collectAsState()
    val tempAdjustment by mainViewModel.tempAdjustment.collectAsState()
    val isRefreshing by mainViewModel.isRefreshing.collectAsState()

    val currentLocation by mainViewModel.currentLocation.collectAsState()
    val cctvInfo by cctvViewModel.cctvInfo.collectAsState()
    val cctvError by cctvViewModel.cctvError.collectAsState()
    val isCctvLoading by cctvViewModel.isLoading.collectAsState()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            mainViewModel.refreshData()
            currentLocation?.let {
                val address = weatherState.address.takeIf { it.isNotBlank() } ?: "현재 위치"
                cctvViewModel.updateSelectedLocation(it.latitude, it.longitude, address, it)
            }
        }
    )

    val scaffoldState = rememberScaffoldState()

    // 현재 위치가 변경되면 CCTV 정보 가져오기
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            if (cctvInfo == null) { // 정보가 없을 때만 자동 로드
                val address = weatherState.address.takeIf { it.isNotBlank() } ?: "현재 위치"
                cctvViewModel.updateSelectedLocation(it.latitude, it.longitude, address, it)
            }
        }
    }

    var isDetailExpanded by remember { mutableStateOf(false) }
    var isForecastExpanded by remember { mutableStateOf(false) }

    // 초기 설정 다이얼로그
    if (showSetupDialog) {
        TemperaturePreferenceDialog(
            currentValue = tempAdjustment,
            onDismiss = { /* 초기 설정이므로 닫기 액션 없음 */ },
            onConfirm = { adjustment ->
                mainViewModel.saveTempAdjustment(adjustment)
            }
        )
    }

    // 재설정 다이얼로그
    if (showTempAdjustmentDialog) {
        TemperaturePreferenceDialog(
            currentValue = tempAdjustment,
            onDismiss = { mainViewModel.closeTempAdjustmentDialog() },
            onConfirm = { adjustment ->
                mainViewModel.saveTempAdjustment(adjustment)
            }
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            WeatherTopAppBar(
                viewModel = mainViewModel,
                searchViewModel = searchViewModel,
                cctvViewModel = cctvViewModel,
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
            // 날씨 정보 또는 로딩 화면
            AnimatedVisibility(visible = weatherState.error == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
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
                        currentWeather = weatherState.currentWeather,
                        weatherDetails = weatherState.weatherDetails,
                        hourlyForecast = weatherState.hourlyForecast,
                        feelsLike = weatherState.currentWeather.feelsLike,
                        tempAdjustment = tempAdjustment,
                        onSettingsClick = { mainViewModel.openTempAdjustmentDialog() }
                    )
                    
                    NearbyCctvCard(
                        isLoading = isCctvLoading,
                        cctvInfo = cctvInfo,
                        error = cctvError,
                        onMoreClick = { navController.navigate("cctv") },
                        onCctvClick = { cctv ->
                            val encodedUrl = android.util.Base64.encodeToString(cctv.cctvUrl.toByteArray(), android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
                            navController.navigate("cctvPlayer/${cctv.cctvName}/$encodedUrl")
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.data_source_credit),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = weatherState.lastUpdated,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = Color.White,
                contentColor = Color(0xFF2563EB),
                scale = false
            )

            // 오류 메시지 표시
            AnimatedVisibility(
                visible = weatherState.error != null,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    backgroundColor = Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "오류",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = weatherState.error ?: "알 수 없는 오류가 발생했습니다.",
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { mainViewModel.refreshData() }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun TemperaturePreferenceDialog(currentValue: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var sliderPosition by remember { mutableStateOf(currentValue.toFloat()) }

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
                    onValueChange = { sliderPosition = it.roundToInt().toFloat() },
                    valueRange = -3f..3f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colors.primary,
                        activeTrackColor = MaterialTheme.colors.primary,
                        inactiveTrackColor = Color.LightGray
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