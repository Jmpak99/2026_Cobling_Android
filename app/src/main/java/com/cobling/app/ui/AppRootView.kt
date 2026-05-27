package com.cobling.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cobling.app.navigation.Screen
import com.cobling.app.ui.auth.EmailSignupScreen
import com.cobling.app.ui.auth.LoginScreen
import com.cobling.app.ui.auth.ResetPasswordScreen
import com.cobling.app.ui.auth.SignupScreen
import com.cobling.app.ui.auth.SplashScreen
import com.cobling.app.ui.experience.ExperienceQuestScreen
import com.cobling.app.ui.journal.JournalMemoryFragmentScreen
import com.cobling.app.ui.onboarding.OnboardingScreen
import com.cobling.app.ui.quest.QuestBlockScreen
import com.cobling.app.ui.quest.QuestDetailScreen
import com.cobling.app.ui.settings.ContributionFormScreen
import com.cobling.app.ui.settings.ContributionThanksScreen
import com.cobling.app.ui.settings.EditProfileScreen
import com.cobling.app.ui.settings.membership.PremiumSubscriptionScreen
import com.cobling.app.ui.shared.RootTabContainer
import com.cobling.app.viewmodel.AppStateViewModel
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.TabBarViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun AppRootView() {
    val appState = hiltViewModel<AppStateViewModel>()
    val tabBarViewModel = hiltViewModel<TabBarViewModel>()
    val authViewModel = hiltViewModel<AuthViewModel>()
    val navController = rememberNavController()

    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences("cobling_prefs", Context.MODE_PRIVATE)
    }

    val isOnboardingDoneState = remember {
        mutableStateOf(prefs.getBoolean("onboarding_done", false))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!appState.isSplashDone) {
            SplashScreen(
                onSplashDone = {
                    appState.isSplashDone = true
                }
            )
            return@Box
        }

        NavHost(
            navController = navController,
            startDestination = if (isOnboardingDoneState.value) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }
        ) {
            // ── 온보딩 ──────────────────────────────────────────────────────
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinish = {
                        prefs.edit()
                            .putBoolean("onboarding_done", true)
                            .apply()

                        navController.navigate(Screen.Signup.route) {
                            popUpTo(Screen.Onboarding.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── 메인 탭 ──────────────────────────────────────────────────────
            composable(Screen.Home.route) {
                RootTabContainer(
                    appState = appState,
                    tabBarViewModel = tabBarViewModel,
                    authViewModel = authViewModel,
                    navController = navController
                )
            }

            // ── 인증 ─────────────────────────────────────────────────────────
            composable(Screen.Signup.route) {
                SignupScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onTapLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Signup.route) {
                                inclusive = true
                            }
                        }
                    },
                    onTapEmailSignup = {
                        navController.navigate(Screen.EmailSignup.route)
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    },
                    onTapSignup = {
                        navController.navigate(Screen.EmailSignup.route)
                    },
                    onTapResetPassword = {
                        navController.navigate(Screen.ResetPassword.route)
                    }
                )
            }

            composable(Screen.EmailSignup.route) {
                EmailSignupScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSignupSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    },
                    onTapLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.EmailSignup.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(Screen.ResetPassword.route) {
                ResetPasswordScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ── 퀘스트 상세 ──────────────────────────────────────────────────
            composable(Screen.QuestDetail.route) { backStack ->
                val chapterId = backStack.arguments?.getString("chapterId")
                    ?: return@composable

                QuestDetailScreen(
                    chapterId = chapterId,
                    tabBarViewModel = tabBarViewModel,
                    authViewModel = authViewModel,
                    onNavigateToGame = { subQuestId ->
                        navController.navigate(
                            Screen.QuestBlock.createRoute(
                                chapterId = chapterId,
                                subQuestId = subQuestId
                            )
                        )
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ── 게임 화면 ────────────────────────────────────────────────────
            composable(Screen.QuestBlock.route) { backStack ->
                val chapterId = backStack.arguments?.getString("chapterId")
                    ?: return@composable

                val subQuestId = backStack.arguments?.getString("subQuestId")
                    ?: return@composable

                QuestBlockScreen(
                    chapterId = chapterId,
                    subQuestId = subQuestId,
                    tabBarViewModel = tabBarViewModel,
                    authViewModel = authViewModel,
                    onGoNextSubQuest = { nextId ->
                        navController.navigate(
                            Screen.QuestBlock.createRoute(
                                chapterId = chapterId,
                                subQuestId = nextId
                            )
                        ) {
                            popUpTo(Screen.QuestBlock.route) {
                                inclusive = true
                            }
                        }
                    },
                    onExitToList = {
                        tabBarViewModel.isTabBarVisible = true
                        navController.popBackStack(
                            route = Screen.Home.route,
                            inclusive = false
                        )
                    }
                )
            }

            // ── 설정 하위 ────────────────────────────────────────────────────
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    authViewModel = authViewModel,
                    tabBarViewModel = tabBarViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onAccountDeleted = {
                        tabBarViewModel.isTabBarVisible = true

                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.ContributionThanks.route) {
                ContributionThanksScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ContributionForm.route) {
                ContributionFormScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.PremiumSubscription.route) {
                PremiumSubscriptionScreen(
                    authViewModel = authViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ── 기록장 하위 ──────────────────────────────────────────────────
            composable(Screen.JournalMemoryFragment.route) {
                JournalMemoryFragmentScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onHiddenQuestClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid

                        if (userId != null) {
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(userId)
                                .update(
                                    mapOf(
                                        "hiddenChapters.hidden1.showInQuestList" to true
                                    )
                                )
                        }

                        navController.navigate(
                            Screen.QuestDetail.createRoute("hidden1")
                        )
                    }
                )
            }

            // ── 체험 ─────────────────────────────────────────────────────────
            composable(Screen.Experience.route) {
                ExperienceQuestScreen(
                    appState = appState,
                    navController = navController
                )
            }
        }
    }
}