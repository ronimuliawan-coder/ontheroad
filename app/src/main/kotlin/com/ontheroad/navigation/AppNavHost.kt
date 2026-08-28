package com.ontheroad.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ontheroad.OnTheRoadApplication
import com.ontheroad.ui.history.HistoryScreen
import com.ontheroad.ui.settings.SettingsScreen
import com.ontheroad.ui.shift.ShiftScreen
import com.ontheroad.ui.tracker.TrackerScreen
import com.ontheroad.viewmodel.HistoryViewModel
import com.ontheroad.viewmodel.ShiftViewModel
import com.ontheroad.viewmodel.TrackerViewModel
import com.ontheroad.viewmodel.ViewModelFactory

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as OnTheRoadApplication
    val factory = ViewModelFactory(app)

    NavHost(
        navController = navController,
        startDestination = Screen.Tracker.route,
        modifier = modifier
    ) {
        composable(Screen.Tracker.route) {
            val trackerViewModel: TrackerViewModel = viewModel(factory = factory)
            TrackerScreen(viewModel = trackerViewModel)
        }
        composable(Screen.Shift.route) {
            val shiftViewModel: ShiftViewModel = viewModel(factory = factory)
            ShiftScreen(viewModel = shiftViewModel)
        }
        composable(Screen.History.route) {
            val historyViewModel: HistoryViewModel = viewModel(factory = factory)
            HistoryScreen(viewModel = historyViewModel)
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
