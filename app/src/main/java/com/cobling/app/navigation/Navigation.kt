package com.cobling.app.navigation

// Cobling 앱 네비게이션 경로 정의
// Swift의 NavigationStack/NavigationRouter → Android Navigation Component

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Splash : Screen("splash")
    object Signup : Screen("signup")
    object ResetPassword : Screen("reset_password")
    object Login : Screen("login")
    object EmailSignup : Screen("email_signup")
    object Home : Screen("home")
    object GuestHome : Screen("guest_home")
    object QuestList : Screen("quest_list")

    object QuestDetail : Screen("quest_detail/{chapterId}") {
        fun createRoute(chapterId: String) = "quest_detail/$chapterId"
    }

    object QuestBlock : Screen("quest_block/{chapterId}/{subQuestId}") {
        fun createRoute(chapterId: String, subQuestId: String) =
            "quest_block/$chapterId/$subQuestId"
    }

    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")

    object ContributionThanks : Screen("contribution_thanks")
    object ContributionForm : Screen("contribution_form")

    object CutscenePreview : Screen("cutscene_preview")

    object Experience : Screen("experience")
    object LearnMore : Screen("learn_more")
    object PremiumSubscription : Screen("premium_subscription")
    object PrivacyPolicy : Screen("privacy_policy")
    object AppInfo : Screen("app_info")
    object PushSetting : Screen("push_setting")
    object JournalMemoryFragment : Screen("journal_memory_fragment")
}