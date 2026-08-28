package com.ontheroad.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.ontheroad.R

sealed class Screen(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    data object Tracker : Screen("tracker", R.string.nav_tracker, Icons.Default.PlayArrow)
    data object Shift : Screen("shift", R.string.nav_shift, Icons.Default.DateRange)
    data object History : Screen("history", R.string.nav_history, Icons.AutoMirrored.Filled.List)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Tracker, Shift, History, Settings)
    }
}
