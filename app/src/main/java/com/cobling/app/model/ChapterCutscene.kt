package com.cobling.app.model

// ──────────────────────────────────────
// ChapterCutsceneType
// ──────────────────────────────────────
enum class ChapterCutsceneType(val rawValue: String) {
    INTRO("intro"),
    OUTRO("outro");

    val primaryButtonTitle: String get() = when (this) {
        INTRO -> "시작하기"
        OUTRO -> "계속하기"
    }

    companion object {
        fun fromRawValue(raw: String): ChapterCutsceneType? =
            entries.firstOrNull { it.rawValue == raw }
    }
}

// ──────────────────────────────────────
// ChapterCutscene
// ──────────────────────────────────────
data class ChapterCutscene(
    val chapterId: String,
    val type: ChapterCutsceneType,
    val lines: List<DialogueLine>,
    val backgroundAssetName: String? = null,
    val coblingAssetName: String? = null,
    val spiritAssetName: String? = null
) {
    val id: String get() = "${chapterId}_${type.rawValue}"
}

// ──────────────────────────────────────
// ChapterCutsceneProvider
// ──────────────────────────────────────
object ChapterCutsceneProvider {
    fun introCutscene(chapterId: String): ChapterCutscene {
        return ChapterCutscene(
            chapterId = chapterId,
            type = ChapterCutsceneType.INTRO,
            lines = ChapterDialogueStore.lines(chapterId, ChapterCutsceneType.INTRO),
            backgroundAssetName = backgroundAssetName(chapterId),
            coblingAssetName = "cobling_stage_egg",
            spiritAssetName = "spirit_forest"
        )
    }

    fun outroCutscene(chapterId: String): ChapterCutscene {
        return ChapterCutscene(
            chapterId = chapterId,
            type = ChapterCutsceneType.OUTRO,
            lines = ChapterDialogueStore.lines(chapterId, ChapterCutsceneType.OUTRO),
            backgroundAssetName = backgroundAssetName(chapterId),
            coblingAssetName = "cobling_stage_egg",
            spiritAssetName = "spirit_forest"
        )
    }

    private fun backgroundAssetName(chapterId: String): String? = when (chapterId.lowercase()) {
        "ch1" -> "bg_ch1_intro"
        else -> null
    }
}
