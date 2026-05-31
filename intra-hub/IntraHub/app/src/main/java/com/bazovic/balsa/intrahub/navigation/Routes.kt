package com.bazovic.balsa.intrahub.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// ─── SECTION: Route Definitions For Navigation 3 ─── //
@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object HomeRoute : AppRoute

    @Serializable
    data object ScheduleRoute : AppRoute

    @Serializable
    data object StandingsRoute : AppRoute

    @Serializable
    data object TeamsRoute : AppRoute

    @Serializable
    data object ProfileRoute : AppRoute
}

@Serializable
data class GameDetailRoute(val gameId: String) : AppRoute

@Serializable
data class TeamDetailRoute(val teamId: String) : AppRoute

// ─── SECTION: Helpers ─── //
fun AppRoute.isTabRoute(): Boolean = when (this) {
    is AppRoute.HomeRoute,
    is AppRoute.ScheduleRoute,
    is AppRoute.StandingsRoute,
    is AppRoute.TeamsRoute,
    is AppRoute.ProfileRoute -> true
    else -> false
}//AppRoute.isTabRoute

val TAB_ORDER: List<AppRoute> = listOf(
    AppRoute.HomeRoute,
    AppRoute.ScheduleRoute,
    AppRoute.StandingsRoute,
    AppRoute.TeamsRoute,
    AppRoute.ProfileRoute
)//TAB_ORDER
