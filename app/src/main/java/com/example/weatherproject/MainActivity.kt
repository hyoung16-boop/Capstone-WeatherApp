// MainActivity.kt (10단계: '주간 예보' 한 줄 버그까지 수정한 최종본)

package com.example.weatherproject

import androidx.activity.viewModels
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.NotificationCompat // showFullScreenNotification에서 사용될 수 있음
import androidx.core.content.ContextCompat // showFullScreenNotification에서 사용될 수 있음
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
import androidx.compose.foundation.lazy.LazyColumn // LazyColumn 임포트 추가
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

import coil.compose.AsyncImage
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.ui.MainViewModel

import com.example.weatherproject.ui.theme.WeatherProjectTheme
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.zIndex
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherproject.ui.WeatherNavHost


class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current



            WeatherProjectTheme {
                val weatherState by viewModel.uiState.collectAsState()
                // Navigation.kt의 WeatherNavHost를 직접 호출하는 대신, 여기에서 NavController를 생성하고 WeatherApp으로 전달
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF60A5FA) // 연한 파란색
                ) {
                    WeatherNavHost(weatherState = weatherState, viewModel = viewModel)
                }
            }
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

