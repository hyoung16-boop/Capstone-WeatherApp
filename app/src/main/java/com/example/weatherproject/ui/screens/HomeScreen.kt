package com.example.weatherproject.ui.screens

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
    // 최초 설문 다이얼로그 표시 여부
    val showDialog by viewModel.showSetupDialog.collectAsState()
    // 저장된 체질 보정값
    val tempAdjustment by viewModel.tempAdjustment.collectAsState()
    
        // 새로고침 상태
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val pullRefreshState = rememberPullRefreshState(isRefreshing, { viewModel.refreshData() }) // ⬅️ refreshData()로 변경
    
        if (showDialog) {
            TemperaturePreferenceDialog(onDismiss = { /* 강제 설정 유도 (취소 불가) */ }) { adjustment ->
                viewModel.saveTempAdjustment(adjustment)
            }
        }
    
        Scaffold(
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
    
                                    // ⭐️ 개선된 CCTV 카드 (주변 2개만 보여주기)
                                    NearbyCctvCard(
                                        cctvList = weatherState.cctvList.take(2), // 상위 2개만 전달
                                        onMoreClick = { navController.navigate("cctv") },
                                        onCctvClick = { cctv -> 
                                            // TODO: 썸네일 클릭 시 해당 CCTV의 전체 화면/스트리밍 화면으로 이동
                                            // 예: navController.navigate("cctv_detail/${cctv.id}")
                                        }
                                    )                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
    
                // 새로고침 인디케이터 (디자인 개선)
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color.White, // 배경은 깔끔한 흰색
                    contentColor = Color(0xFF2563EB) // 아이콘은 진한 파란색
                )
            }
        }}

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

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    // Preview
}