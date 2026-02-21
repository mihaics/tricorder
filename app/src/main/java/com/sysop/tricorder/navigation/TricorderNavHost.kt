package com.sysop.tricorder.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.feature.map.MapScreen

@Composable
fun TricorderNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
    ) {
        composable(Screen.Map.route) {
            MapScreen(
                onNavigateToDetail = { category ->
                    navController.navigate("detail/${category.name}")
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToSessions = {
                    navController.navigate(Screen.Sessions.route)
                },
            )
        }

        composable(
            route = Screen.DETAIL_ROUTE,
            arguments = listOf(navArgument("category") { type = NavType.StringType }),
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category") ?: return@composable
            val category = SensorCategory.valueOf(categoryName)
            // Detail views will be added in Phase 7
            // For now, placeholder
            androidx.compose.material3.Text("Detail: ${category.displayName}")
        }

        composable(Screen.Sessions.route) {
            androidx.compose.material3.Text("Sessions")
        }

        composable(
            route = Screen.SESSION_REPLAY_ROUTE,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) {
            androidx.compose.material3.Text("Session Replay")
        }

        composable(Screen.Settings.route) {
            androidx.compose.material3.Text("Settings")
        }
    }
}
