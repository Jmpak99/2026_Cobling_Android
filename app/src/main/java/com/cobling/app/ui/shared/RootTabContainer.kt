package com.cobling.app.ui.shared

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.cobling.app.navigation.Screen
import com.cobling.app.ui.Ranking.RankingScreen
import com.cobling.app.ui.home.GuestHomeScreen
import com.cobling.app.ui.home.HomeScreen
import com.cobling.app.ui.journal.JournalScreen
import com.cobling.app.ui.quest.QuestListScreen
import com.cobling.app.ui.settings.SettingsScreen
import com.cobling.app.viewmodel.AppState
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.TabBarViewModel
import com.cobling.app.viewmodel.TabItem

@Composable
fun RootTabContainer(
    appState: AppState,
    tabBarViewModel: TabBarViewModel,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    val isSignedIn by authViewModel.isSignedIn.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        // 탭 콘텐츠
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = if (tabBarViewModel.isTabBarVisible && !appState.isInGame) {
                        100.dp
                    } else {
                        0.dp
                    }
                )
        ) {
            when (appState.selectedTab) {
                TabItem.HOME -> {
                    if (isSignedIn) {
                        HomeScreen(
                            appState = appState,
                            authViewModel = authViewModel
                        )
                    } else {
                        GuestHomeScreen(
                            appState = appState,
                            onNavigateToSignup = {
                                navController.navigate(Screen.Signup.route)
                            }
                        )
                    }
                }

                TabItem.QUEST -> {
                    if (isSignedIn) {
                        QuestListScreen(
                            appState = appState,
                            tabBarViewModel = tabBarViewModel,
                            navController = navController
                        )
                    } else {
                        GuestHomeScreen(
                            appState = appState,
                            onNavigateToSignup = {
                                navController.navigate(Screen.Signup.route)
                            }
                        )
                    }
                }

                TabItem.JOURNAL -> {
                    if (isSignedIn) {
                        JournalScreen(
                            onMemoryMoreClick = {
                                navController.navigate(Screen.JournalMemoryFragment.route)
                            }
                        )
                    } else {
                        GuestHomeScreen(
                            appState = appState,
                            onNavigateToSignup = {
                                navController.navigate(Screen.Signup.route)
                            }
                        )
                    }
                }

                TabItem.RANKING -> {
                    if (isSignedIn) {
                        RankingScreen()
                    } else {
                        GuestHomeScreen(
                            appState = appState,
                            onNavigateToSignup = {
                                navController.navigate(Screen.Signup.route)
                            }
                        )
                    }
                }

                TabItem.PROFILE -> {
                    if (isSignedIn) {
                        SettingsScreen(
                            authViewModel = authViewModel,
                            tabBarViewModel = tabBarViewModel,
                            navController = navController
                        )
                    } else {
                        GuestHomeScreen(
                            appState = appState,
                            onNavigateToSignup = {
                                navController.navigate(Screen.Signup.route)
                            }
                        )
                    }
                }
            }
        }

        // 플로팅 탭바
        if (tabBarViewModel.isTabBarVisible && !appState.isInGame) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                FloatingTabBar(
                    selectedTab = appState.selectedTab,
                    onTabSelected = { tab ->
                        appState.selectedTab = tab
                    },
                    authViewModel = authViewModel,
                    navController = navController
                )
            }
        }
    }
}