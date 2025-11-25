package com.example.weatherproject.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weatherproject.WeatherApp
import com.example.weatherproject.data.WeatherState

@Composable
fun WeatherNavHost(weatherState: WeatherState) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            WeatherApp(
                weatherState = weatherState,
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(navController = navController)
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
