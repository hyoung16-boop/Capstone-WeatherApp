package com.example.weatherproject

import androidx.activity.viewModels
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

import coil.compose.AsyncImage
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.ui.MainViewModel
import com.example.weatherproject.ui.SearchViewModel

import com.example.weatherproject.ui.theme.WeatherProjectTheme
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherproject.ui.WeatherNavHost

import androidx.compose.ui.graphics.Brush

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()

    // 위치 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            mainViewModel.getCurrentLocationOnce()
            mainViewModel.startLocationTracking()
            Toast.makeText(this, "위치 권한이 허용되었습니다", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 앱 시작 시 위치 권한 확인 및 요청
        checkAndRequestLocationPermission()

        setContent {
            val context = LocalContext.current

            WeatherProjectTheme {
                val weatherState by mainViewModel.uiState.collectAsState()

                val backgroundBrush = getWeatherGradient(weatherState.currentWeather.iconUrl)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = backgroundBrush)
                ) {
                    WeatherNavHost(
                        weatherState = weatherState,
                        viewModel = mainViewModel,
                        searchViewModel = searchViewModel
                    )
                }
            }
        }
    }

    private fun checkAndRequestLocationPermission() {
        when {
            mainViewModel.hasLocationPermission() -> {
                mainViewModel.getCurrentLocationOnce()
                mainViewModel.startLocationTracking()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // 날씨 상태(아이콘 URL 등)에 따른 그라데이션 반환
    private fun getWeatherGradient(iconUrl: String): Brush {
        return when {
            iconUrl.contains("01") || iconUrl.contains("sunny") -> { // 맑음 (Sunny)
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4FC3F7), // 상단: 밝은 하늘색
                        Color(0xFF81D4FA)  // 하단: 더 연한 하늘색
                    )
                )
            }
            iconUrl.contains("02") || iconUrl.contains("03") || iconUrl.contains("04") || iconUrl.contains("cloud") -> { // 흐림 (Cloudy)
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF78909C), // 상단: 회색빛 파랑
                        Color(0xFF90A4AE)  // 하단: 흐린 회색
                    )
                )
            }
            iconUrl.contains("09") || iconUrl.contains("10") || iconUrl.contains("rain") -> { // 비 (Rain)
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF37474F), // 상단: 어두운 남색
                        Color(0xFF546E7A)  // 하단: 짙은 회색
                    )
                )
            }
            iconUrl.contains("11") || iconUrl.contains("13") || iconUrl.contains("50") -> { // 뇌우, 눈, 안개 등
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF455A64),
                        Color(0xFF607D8B)
                    )
                )
            }
            else -> { // 기본 (Default) - 기존 파란색 계열 유지
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF60A5FA),
                        Color(0xFF93C5FD)
                    )
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 설정 화면 갔다 왔을 때 다시 체크 (선택 사항)
    }

    private fun checkLocationSettings() {
        if (!com.example.weatherproject.util.LocationPermissionHelper.hasLocationPermission(this)) {
            com.example.weatherproject.util.LocationPermissionHelper.requestLocationPermission(this)
        } else if (!com.example.weatherproject.util.LocationPermissionHelper.isGpsEnabled(this)) {
            com.example.weatherproject.util.LocationPermissionHelper.showGpsSettingDialog(this)
        }
    }

    companion object {
        // This function creates and shows the full-screen notification
        fun showFullScreenNotification(context: Context, weatherState: WeatherState) {
            // Prepare notification content
            val weather = weatherState.currentWeather
            val notificationContent = "기온: %s (체감: %s) | 상태: %s".format(
                weather.temperature,
                weather.feelsLike,
                weather.description
            )

            // Use the helper class
            com.example.weatherproject.util.NotificationHelper.showNotification(context, "현재 날씨", notificationContent)
        }
    }
}