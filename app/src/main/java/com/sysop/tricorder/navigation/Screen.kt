package com.sysop.tricorder.navigation

import com.sysop.tricorder.core.model.SensorCategory

sealed class Screen(val route: String) {
    data object Map : Screen("map")
    data class Detail(val category: SensorCategory) : Screen("detail/${category.name}")
    data object Sessions : Screen("sessions")
    data class SessionReplay(val sessionId: String) : Screen("session_replay/$sessionId")
    data object Settings : Screen("settings")

    companion object {
        const val DETAIL_ROUTE = "detail/{category}"
        const val SESSION_REPLAY_ROUTE = "session_replay/{sessionId}"
    }
}
