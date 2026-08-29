package com.ontheroad

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ontheroad.core.ui.theme.OnTheRoadTheme
import com.ontheroad.navigation.AppNavHost
import com.ontheroad.navigation.Screen
import com.ontheroad.viewmodel.SettingsViewModel
import com.ontheroad.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupHighRefreshRateDisplay()
        setContent {
            val context = LocalContext.current
            val app = context.applicationContext as OnTheRoadApplication
            val factory = ViewModelFactory(app)
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            val themeMode by settingsViewModel.themeMode.collectAsState()

            OnTheRoadTheme(themeMode = themeMode) {
                OnTheRoadAppShell()
            }
        }
    }

    /**
     * Queries display capabilities and requests the maximum supported refresh rate (e.g. 120Hz or 90Hz)
     * on supported Android 11+ (API 30+) devices for fluid cockpit rendering.
     */
    private fun setupHighRefreshRateDisplay() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = display
            val supportedModes = display?.supportedModes
            val maxMode = supportedModes?.maxByOrNull { it.refreshRate }
            if (maxMode != null && maxMode.refreshRate > 60f) {
                val params = window.attributes
                params.preferredDisplayModeId = maxMode.modeId
                window.attributes = params
            }
        }
    }
}

@Composable
fun OnTheRoadAppShell() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
        Screen.Tracker,
        Screen.Shift,
        Screen.History,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navigationItems.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(screen.titleRes)
                            )
                        },
                        label = {
                            Text(text = stringResource(screen.titleRes))
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavHost(navController = navController)
        }
    }
}
