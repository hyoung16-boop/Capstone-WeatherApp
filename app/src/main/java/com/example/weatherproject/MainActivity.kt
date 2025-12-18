package com.example.weatherproject

import androidx.activity.viewModels
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.Manifest
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.ui.CctvViewModel
import com.example.weatherproject.ui.MainViewModel
import com.example.weatherproject.ui.SearchViewModel
import com.example.weatherproject.ui.WeatherNavHost
import com.example.weatherproject.ui.theme.WeatherProjectTheme
import com.example.weatherproject.util.LocationPermissionHelper
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val searchViewModel: SearchViewModel by viewModels()
    private val cctvViewModel: CctvViewModel by viewModels()

    // 위치 권한 요청 런처
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 권한이 허용되면 GPS 상태를 다시 확인
            checkGpsAndFetchLocation()
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
                        mainViewModel = mainViewModel,
                        searchViewModel = searchViewModel,
                        cctvViewModel = cctvViewModel
                    )
                }
            }
        }
    }
    
    private fun checkAndRequestLocationPermission() {
        if (mainViewModel.hasLocationPermission()) {
            checkGpsAndFetchLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkGpsAndFetchLocation() {
        if (LocationPermissionHelper.isGpsEnabled(this)) {
            mainViewModel.getCurrentLocationOnce()
            mainViewModel.startLocationTracking()
        } else {
            mainViewModel.onGpsDisabled()
            LocationPermissionHelper.showGpsSettingDialog(this)
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
            val weatherDetails = weatherState.weatherDetails
            val pm10Status = com.example.weatherproject.util.PmStatusHelper.getStatus(weatherDetails.pm10)

            val tempValue = weather.temperature.replace("°", "")
            val feelsLikeValue = weather.feelsLike.replace("°", "")

            val notificationContent = "날씨 : %s 기온: %s도(체감온도 : %s도 ) 미세먼지 : %s".format(
                weather.description,
                tempValue,
                feelsLikeValue,
                pm10Status
            )

            // Use the helper class
            com.example.weatherproject.util.NotificationHelper.showNotification(context, "현재 날씨", notificationContent)
        }
    }
}