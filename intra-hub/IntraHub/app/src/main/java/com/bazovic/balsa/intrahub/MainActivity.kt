package com.bazovic.balsa.intrahub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.bazovic.balsa.intrahub.navigation.*
import com.bazovic.balsa.intrahub.ui.screens.*
import com.bazovic.balsa.intrahub.ui.theme.Ink5
import com.bazovic.balsa.intrahub.ui.theme.IntraHubTheme
import com.bazovic.balsa.intrahub.ui.theme.OrangeRIT
import com.bazovic.balsa.intrahub.ui.theme.OrangeTint
import com.bazovic.balsa.intrahub.viewmodel.AppState
import com.bazovic.balsa.intrahub.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IntraHubTheme {
                IntraHubApp()
            }//IntraHubTheme
        }//setContent
    }////onCreate
}//MainActivity

@Composable
private fun IntraHubApp(appVm: AppViewModel = viewModel()) {
    when (appVm.appState) {
        AppState.Login -> LoginScreen(
            onLogin = { u, p ->
                appVm.login(u, p)
            },
            isLoading = appVm.isLoading,
            errorMessage = appVm.errorMessage,
        )//Login

        AppState.Loading -> LoadingScreen(
            onLoadingComplete = appVm::onLoadingComplete,
        )//Loading

        AppState.App -> {
            val user = appVm.currentUser ?: return

            MainNavigation(
                user = user,
                onLogout = appVm::logout,
            )//MainNavigation
        }//App
    }//when
} // IntraHubApp

// ─── SECTION: Main Navigation Shell (Navigation 3) ─── //

@Composable
private fun MainNavigation(
    user: com.bazovic.balsa.intrahub.data.UserProfile,
    onLogout: () -> Unit,
) {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(AppRoute.HomeRoute)

    val currentTop = backStack.lastOrNull() as? AppRoute
    val showBottomNav = currentTop?.isTabRoute() ?: true
    val currentTab = (backStack.lastOrNull {
        (it as? AppRoute)?.isTabRoute() == true } as? AppRoute
    ) ?: AppRoute.HomeRoute

    fun switchTab(route: AppRoute) {
        while (backStack.size > 1) backStack.removeLastOrNull()

        if (backStack.isNotEmpty() && backStack.last() == route) return

        if (backStack.isNotEmpty()) backStack.removeLastOrNull()

        backStack.add(route)
    }//switchTab

    fun pushRoute(route: AppRoute) = backStack.add(route)

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                IntraHubBottomNav(
                    currentRoute = currentTab,
                    onSelect = ::switchTab,
                )//IntraHubBottomNav
            }//if
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) backStack.removeLastOrNull()
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator()
            ),
            entryProvider = entryProvider {
                entry<AppRoute.HomeRoute> {
                    HomeScreen(
                        user = user,
                        onNavGame = { id ->
                            pushRoute(GameDetailRoute(id))
                        },
                        onNavTeam = { id ->
                            pushRoute(TeamDetailRoute(id))
                        },
                        onNavSchedule = {
                            switchTab(AppRoute.ScheduleRoute)
                        },
                        onNavStandings = {
                            switchTab(AppRoute.StandingsRoute)
                        },
                    )//HomeScreen
                }//entry<AppRoute.HomeRoute>

                entry<AppRoute.ScheduleRoute> {
                    ScheduleScreen(
                        user = user,
                        onNavGame = { id ->
                            pushRoute(GameDetailRoute(id))
                        },
                    )//ScheduleScreen
                }//entry<AppRoute.HomeRoute>

                entry<AppRoute.StandingsRoute> {
                    StandingsScreen(
                        onNavTeam = { id ->
                            pushRoute(TeamDetailRoute(id))
                        },
                    )//StandingsScreen
                }//entry<AppRoute.HomeRoute>

                entry<AppRoute.TeamsRoute> {
                    TeamsScreen(
                        user = user,
                        onNavTeam = { id ->
                            pushRoute(TeamDetailRoute(id))
                        },
                    )//TeamsScreen
                }//entry<AppRoute.HomeRoute>

                entry<AppRoute.ProfileRoute> {
                    ProfileScreen(
                        user = user,
                        onLogout = onLogout,
                        onNavTeam = { id ->
                            pushRoute(TeamDetailRoute(id))
                        },
                    )//ProfileScreen
                }//entry<AppRoute.HomeRoute>

                entry<GameDetailRoute> { route ->
                    GameDetailScreen(
                        gameId = route.gameId,
                        user = user,
                        onBack = {
                            if (backStack.size > 1) backStack.removeLastOrNull()
                        },
                    )//GameDetailScreen
                }//entry<AppRoute.HomeRoute>

                entry<TeamDetailRoute> { route ->
                    TeamDetailScreen(
                        teamId = route.teamId,
                        user = user,
                        onBack = {
                            if (backStack.size > 1) backStack.removeLastOrNull()
                        },
                        onNavGame = { id ->
                            pushRoute(GameDetailRoute(id))
                        },
                    )//TeamDetailScreen
                }//entry<AppRoute.HomeRoute>
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )//NavDisplay
    }//Scaffold
}//MainNavigation

// ─── SECTION: Bottom Nav Bar ─── //
private data class NavTab(
    val route: AppRoute,
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
)//NavTab

private val NAV_TABS = listOf(
    NavTab(
        AppRoute.HomeRoute,
        "Home",
        Icons.Filled.Home,
        Icons.Outlined.Home
    ),
    NavTab(
        AppRoute.ScheduleRoute,
        "Schedule",
        Icons.Filled.CalendarMonth,
        Icons.Outlined.CalendarMonth
    ),
    NavTab(
        AppRoute.StandingsRoute,
        "Standings",
        Icons.Filled.EmojiEvents,
        Icons.Outlined.EmojiEvents
    ),
    NavTab(
        AppRoute.TeamsRoute,
        "Teams",
        Icons.Filled.Group,
        Icons.Outlined.Group
    ),
    NavTab(
        AppRoute.ProfileRoute,
        "Profile",
        Icons.Filled.Person,
        Icons.Outlined.Person
    ),
)//NAV_TABS

@Composable
private fun IntraHubBottomNav(
    currentRoute: AppRoute,
    onSelect: (AppRoute) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        NAV_TABS.forEach { tab ->
            val selected = currentRoute == tab.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onSelect(tab.route)
                },
                icon = {
                    Icon(
                        if (selected) tab.iconFilled else tab.iconOutlined,
                        contentDescription = tab.label,
                    )//Icon
                },
                label = {
                    Text(tab.label)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OrangeRIT,
                    selectedTextColor = OrangeRIT,
                    indicatorColor = OrangeTint,
                    unselectedIconColor = Ink5,
                    unselectedTextColor = Ink5,
                ),
            )
        }
    }
}//IntraHubBottomNav
