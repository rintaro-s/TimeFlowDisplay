package com.nbks.mi.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nbks.mi.ui.screens.DashboardScreen
import com.nbks.mi.ui.screens.settings.SettingsScreen

object Destination {
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Destination.DASHBOARD,
    ) {
        composable(Destination.DASHBOARD) {
            DashboardScreen(
                onNavigateToSettings = { navController.navigate(Destination.SETTINGS) }
            )
        }
        composable(Destination.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
