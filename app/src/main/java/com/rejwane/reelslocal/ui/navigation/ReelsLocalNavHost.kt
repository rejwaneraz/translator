package com.rejwane.reelslocal.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rejwane.reelslocal.ui.MainUiState
import com.rejwane.reelslocal.ui.components.BottomNavBar
import com.rejwane.reelslocal.ui.components.BottomTab
import com.rejwane.reelslocal.ui.screens.backup.BackupScreen
import com.rejwane.reelslocal.ui.screens.content.ContentManagerScreen
import com.rejwane.reelslocal.ui.screens.explore.ExploreScreen
import com.rejwane.reelslocal.ui.screens.home.HomeScreen
import com.rejwane.reelslocal.ui.screens.inbox.InboxScreen
import com.rejwane.reelslocal.ui.screens.profile.ProfileScreen
import com.rejwane.reelslocal.ui.screens.search.SearchScreen
import com.rejwane.reelslocal.ui.screens.settings.SettingsScreen
import com.rejwane.reelslocal.ui.screens.video.VideoDetailScreen

private val BOTTOM_BAR_ROUTES = setOf(
    Routes.HOME, Routes.EXPLORE, Routes.INBOX, Routes.PROFILE
)

@Composable
fun ReelsLocalNavHost(
    mainState: MainUiState,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in BOTTOM_BAR_ROUTES

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    unreadCount = mainState.unreadCount,
                    onTabSelected = { tab -> navController.navigateTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenProfile = { userId -> navController.navigate(Routes.profileDetail(userId)) }
                )
            }
            composable(Routes.EXPLORE) {
                ExploreScreen(
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                    onOpenVideo = { videoId -> navController.navigate(Routes.video(videoId)) }
                )
            }
            composable(Routes.INBOX) {
                InboxScreen(
                    onOpenVideo = { videoId -> navController.navigate(Routes.video(videoId)) },
                    onOpenUser = { userId -> navController.navigate(Routes.profileDetail(userId)) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onOpenVideo = { videoId -> navController.navigate(Routes.video(videoId)) },
                    onOpenUser = { userId -> navController.navigate(Routes.profileDetail(userId)) },
                    onEditProfile = { navController.navigate(Routes.SETTINGS) },
                    onSayHi = { userId ->
                        navController.navigate(Routes.profileDetail(userId)) { launchSingleTop = true }
                    }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenUser = { userId -> navController.navigate(Routes.profileDetail(userId)) },
                    onOpenVideo = { videoId -> navController.navigate(Routes.video(videoId)) }
                )
            }
            composable(
                route = Routes.PROFILE_DETAIL,
                arguments = listOf(navArgument(Routes.PROFILE_DETAIL_ARG) { type = NavType.LongType })
            ) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onOpenVideo = { videoId -> navController.navigate(Routes.video(videoId)) },
                    onOpenUser = { userId -> navController.navigate(Routes.profileDetail(userId)) },
                    onEditProfile = { navController.navigate(Routes.SETTINGS) },
                    onSayHi = { userId ->
                        navController.navigate(Routes.profileDetail(userId)) { launchSingleTop = true }
                    }
                )
            }
            composable(
                route = Routes.VIDEO,
                arguments = listOf(navArgument(Routes.VIDEO_ARG) { type = NavType.LongType })
            ) {
                VideoDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { userId -> navController.navigate(Routes.profileDetail(userId)) }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenContentManager = { navController.navigate(Routes.CONTENT_MANAGER) },
                    onOpenBackup = { navController.navigate(Routes.BACKUP) }
                )
            }
            composable(Routes.CONTENT_MANAGER) {
                ContentManagerScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.BACKUP) {
                BackupScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun NavHostController.navigateTab(tab: BottomTab) {
    navigate(tab.route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
