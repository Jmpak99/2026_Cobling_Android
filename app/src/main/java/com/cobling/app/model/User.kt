package com.cobling.app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserCharacter(
    val stage: String = "egg",
    val customization: Map<String, String>? = null,
    val evolutionLevel: Int? = null,
    val evolutionPending: Boolean? = null,
    val evolutionToStage: String? = null
)

data class UserSettings(
    val notificationsEnabled: Boolean = true,
    val darkMode: Boolean = false
)

data class UserPremium(
    val isActive: Boolean = false,
    val plan: String? = null,
    val productId: String? = null,
    val source: String? = null,
    val since: Date? = null,
    val expiresAt: Date? = null,
    val updatedAt: Date? = null
)

data class UserProfile(
    @DocumentId val id: String? = null,
    val nickname: String = "",
    val email: String = "",
    val level: Int? = null,
    val exp: Int? = null,
    val completedQuestCount: Int? = null,
    val profileImageURL: String? = null,
    @ServerTimestamp val createdAt: Date? = null,
    val character: UserCharacter = UserCharacter(),
    val settings: UserSettings = UserSettings(),
    @ServerTimestamp val lastLogin: Date? = null,
    val premium: UserPremium? = null
)