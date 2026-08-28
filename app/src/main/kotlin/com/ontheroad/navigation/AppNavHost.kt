package com.ontheroad.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ontheroad.ui.history.HistoryScreen
import com.ontheroad.ui.settings.SettingsScreen
import com.ontheroad.ui.shift.ShiftScreen
import com.ontheroad.ui.tracker.TrackerScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tracker.route,
        modifier = modifier
    ) {
        composable(Screen.Tracker.route) {
            TrackerScreen()
        }
        composable(Screen.Shift.route) {
            ShiftScreen()
        }
        composable(Screen.History.route) {
            HistoryScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
