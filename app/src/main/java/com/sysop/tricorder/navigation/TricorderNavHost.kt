package com.sysop.tricorder.navigation

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sysop.tricorder.core.model.SensorCategory
import com.sysop.tricorder.feature.detail.airquality.AirQualityScreen
import com.sysop.tricorder.feature.detail.audio.AudioSpectrumScreen
import com.sysop.tricorder.feature.detail.aviation.AircraftTrackerScreen
import com.sysop.tricorder.feature.detail.camera.CameraAnalysisScreen
import com.sysop.tricorder.feature.detail.compass.CompassScreen
import com.sysop.tricorder.feature.detail.environment.BarometerScreen
import com.sysop.tricorder.feature.detail.gnss.GnssSkyPlotScreen
import com.sysop.tricorder.feature.detail.motion.MotionScreen
import com.sysop.tricorder.feature.detail.rf.RfScannerScreen
import com.sysop.tricorder.feature.detail.seismic.SeismicScreen
import com.sysop.tricorder.feature.detail.weather.WeatherScreen
import com.sysop.tricorder.permission.PermissionManager
import com.sysop.tricorder.feature.map.MapScreen
import com.sysop.tricorder.feature.session.list.SessionListScreen
import com.sysop.tricorder.feature.session.replay.SessionReplayScreen
import com.sysop.tricorder.feature.settings.SettingsScreen
import com.sysop.tricorder.permission.PermissionScreen

@Composable
fun TricorderNavHost(
    navController: NavHostController = rememberNavController(),
) {
    val context = LocalContext.current
    val allGranted = PermissionManager.allPermissions().all { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    val startDestination = if (allGranted) Screen.Map.route else Screen.Permissions.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Screen.Permissions.route) {
            PermissionScreen(
                onAllGranted = {
                    navController.navigate(Screen.Map.route) {
                        popUpTo(Screen.Permissions.route) { inclusive = true }
                    }
                },
            )
        }

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
            val onBack = { navController.popBackStack(); Unit }

            when (category) {
                SensorCategory.MOTION -> MotionScreen(onBack = onBack)
                SensorCategory.ENVIRONMENT -> BarometerScreen(onBack = onBack)
                SensorCategory.LOCATION -> GnssSkyPlotScreen(onBack = onBack)
                SensorCategory.RF -> RfScannerScreen(onBack = onBack)
                SensorCategory.AUDIO -> AudioSpectrumScreen(onBack = onBack)
                SensorCategory.CAMERA -> CameraAnalysisScreen(onBack = onBack)
                SensorCategory.AVIATION -> AircraftTrackerScreen(onBack = onBack)
                SensorCategory.WEATHER -> WeatherScreen(onBack = onBack)
                SensorCategory.AIR_QUALITY -> AirQualityScreen(onBack = onBack)
                SensorCategory.SEISMIC -> SeismicScreen(onBack = onBack)
                // Categories without dedicated detail views show compass as fallback
                SensorCategory.RADIATION,
                SensorCategory.SPACE,
                SensorCategory.TIDES -> CompassScreen(onBack = onBack)
            }
        }

        composable(Screen.Sessions.route) {
            SessionListScreen(
                onBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate("session_replay/$sessionId")
                },
            )
        }

        composable(
            route = Screen.SESSION_REPLAY_ROUTE,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) {
            SessionReplayScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
