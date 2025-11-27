package com.example.weatherproject.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weatherproject.data.WeatherState
import com.example.weatherproject.ui.screens.DetailWeatherScreen
import com.example.weatherproject.ui.screens.ForecastScreen
import com.example.weatherproject.ui.screens.HomeScreen
import com.example.weatherproject.ui.screens.CctvScreen

// SettingsScreen, CctvScreen 등은 같은 패키지(ui)에 있는 다른 파일에 정의되어 있으므로
// 별도 import 없이 바로 사용 가능하거나, 필요시 import가 자동으로 처리됩니다.
// 만약 패키지가 다르다면 import가 필요합니다. 
// 확인 결과 모두 `com.example.weatherproject.ui` 패키지에 있으므로 import 불필요.

@Composable
fun WeatherNavHost(
    weatherState: WeatherState, 
    viewModel: MainViewModel,
    searchViewModel: SearchViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                weatherState = weatherState,
                navController = navController,
                viewModel = viewModel,
                searchViewModel = searchViewModel
            )
        }
        composable("detail") {
            DetailWeatherScreen(
                weatherState = weatherState,
                navController = navController
            )
        }
        composable("forecast") {
            ForecastScreen(
                weatherState = weatherState,
                navController = navController
            )
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("cctv") {
            // ViewModel 전달
            CctvScreen(
                navController = navController, 
                viewModel = viewModel,
                searchViewModel = searchViewModel
            )
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