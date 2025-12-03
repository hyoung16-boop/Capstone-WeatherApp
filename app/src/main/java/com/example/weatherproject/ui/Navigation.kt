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
import com.example.weatherproject.ui.screens.CctvPlayerScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun WeatherNavHost(
    weatherState: WeatherState,
    mainViewModel: MainViewModel,
    searchViewModel: SearchViewModel,
    cctvViewModel: CctvViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                weatherState = weatherState,
                navController = navController,
                mainViewModel = mainViewModel,
                searchViewModel = searchViewModel,
                cctvViewModel = cctvViewModel
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
            CctvScreen(
                navController = navController,
                mainViewModel = mainViewModel,
                searchViewModel = searchViewModel,
                cctvViewModel = cctvViewModel
            )
        }


        // ✅ CCTV Player 화면 추가
        composable(
            route = "cctvPlayer/{cctvName}/{cctvUrl}",
            arguments = listOf(
                navArgument("cctvName") { type = NavType.StringType },
                navArgument("cctvUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cctvName = backStackEntry.arguments?.getString("cctvName") ?: ""
            val encodedUrl = backStackEntry.arguments?.getString("cctvUrl") ?: ""

            // ✅ Base64 디코딩
            val cctvUrl = try {
                String(
                    android.util.Base64.decode(encodedUrl, android.util.Base64.URL_SAFE),
                    Charsets.UTF_8
                )
            } catch (e: Exception) {
                ""
            }

            CctvPlayerScreen(
                navController = navController,
                cctvName = cctvName,
                cctvUrl = cctvUrl
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