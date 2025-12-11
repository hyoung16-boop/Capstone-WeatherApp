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
import com.example.weatherproject.ui.components.CurrentWeatherCard

@Composable
fun DetailWeatherScreen(weatherState: WeatherState, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("상세 날씨 정보", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            // ⭐️ address 사용
            CurrentWeatherCard(
                weather = weatherState.currentWeather,
                address = weatherState.address,
                details = weatherState.weatherDetails,
                isExpanded = true,
                onClick = { }
            )
        }
    }
}