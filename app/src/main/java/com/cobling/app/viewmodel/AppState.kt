package com.cobling.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class TabItem {
    QUEST, JOURNAL, HOME, RANKING, PROFILE;

    val iconName: String get() = when (this) {
        QUEST   -> "tab_icon_quest"
        JOURNAL -> "tab_icon_journal"
        HOME    -> "tab_icon_home"
        RANKING -> "tab_icon_ranking"
        PROFILE -> "tab_icon_profile"
    }

    val title: String get() = when (this) {
        QUEST   -> "퀘스트"
        JOURNAL -> "기록장"
        HOME    -> "홈"
        RANKING -> "랭킹"
        PROFILE -> "프로필"
    }
}

@HiltViewModel
class AppStateViewModel @Inject constructor() : ViewModel() {
    var isSplashDone: Boolean by mutableStateOf(false)
    var selectedTab: TabItem by mutableStateOf(TabItem.HOME)
    var isInGame: Boolean by mutableStateOf(false)
}

// 하위 호환을 위해 AppState 타입 별칭
typealias AppState = AppStateViewModel

@HiltViewModel
class TabBarViewModel @Inject constructor() : ViewModel() {
    var isTabBarVisible: Boolean by mutableStateOf(true)
}
