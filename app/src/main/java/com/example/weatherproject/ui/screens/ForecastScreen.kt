package com.example.weatherproject.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.ui.components.HourlyForecastCard
import com.example.weatherproject.ui.components.WeeklyForecastCard

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
            // 시간별 예보 (주간 예보 포함)
            HourlyForecastCard(
                hourlyForecasts = weatherState.hourlyForecast,
                weeklyForecasts = weatherState.weeklyForecast, // 주간 예보 전달
                isExpanded = true, // 예보 화면이므로 항상 펼침
                onClick = { } 
            )
            
            // 기존 WeeklyForecastCard는 제거
        }
    }
}
