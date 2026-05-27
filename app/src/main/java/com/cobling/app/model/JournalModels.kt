package com.cobling.app.model

data class JournalMemoryFragment(
    val id: String,
    val title: String,
    val isCollected: Boolean
)

data class JournalBadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val imageName: String = "",
    val isUnlocked: Boolean
)

data class BadgeRule(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageName: String = "",
    val type: String = "",
    val order: Long = 0,
    val isActive: Boolean = true
)