package com.cobling.app.model

import java.util.UUID

// ──────────────────────────────────────
// DialogueSpeaker
// ──────────────────────────────────────
enum class DialogueSpeaker(val rawValue: String) {
    COBLING("cobling"),
    SPIRIT("spirit");

    val displayName: String get() = when (this) {
        COBLING -> "코블링"
        SPIRIT  -> "숲의 정령"
    }

    val isLeftSide: Boolean get() = when (this) {
        COBLING -> true
        SPIRIT  -> false
    }

    companion object {
        fun fromRawValue(raw: String): DialogueSpeaker? =
            entries.firstOrNull { it.rawValue == raw }
    }
}

// ──────────────────────────────────────
// DialogueLine
// ──────────────────────────────────────
data class DialogueLine(
    val id: String = UUID.randomUUID().toString(),
    val speaker: DialogueSpeaker,
    val text: String
)
