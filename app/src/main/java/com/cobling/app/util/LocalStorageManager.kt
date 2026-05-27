package com.cobling.app.util

import android.content.Context
import android.content.SharedPreferences
import com.cobling.app.model.ChapterCutsceneType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("cobling_prefs", Context.MODE_PRIVATE)
    }

    private fun key(raw: String) = "cobling.$raw"

    fun setBool(value: Boolean, forRawKey: String) =
        prefs.edit().putBoolean(key(forRawKey), value).apply()

    fun getBool(forRawKey: String, default: Boolean = false): Boolean {
        if (!prefs.contains(key(forRawKey))) return default
        return prefs.getBoolean(key(forRawKey), default)
    }

    fun remove(rawKey: String) = prefs.edit().remove(key(rawKey)).apply()

    private fun cutsceneKey(chapterId: String, type: ChapterCutsceneType) =
        "cutscene_shown_${type.rawValue}_${chapterId.lowercase()}"

    fun isCutsceneShown(chapterId: String, type: ChapterCutsceneType): Boolean =
        getBool(cutsceneKey(chapterId, type))

    fun setCutsceneShown(chapterId: String, type: ChapterCutsceneType) =
        setBool(true, cutsceneKey(chapterId, type))

    fun clearCutsceneShown(chapterId: String, type: ChapterCutsceneType) =
        remove(cutsceneKey(chapterId, type))

    // ─── 전역 접근용 (Composable 외부 / static 호출) ─────────────────────────
    companion object {
        @Volatile private var instance: LocalStorageManager? = null

        fun init(mgr: LocalStorageManager) { instance = mgr }

        fun isCutsceneShown(chapterId: String, type: ChapterCutsceneType): Boolean =
            instance?.isCutsceneShown(chapterId, type) ?: false

        fun setCutsceneShown(chapterId: String, type: ChapterCutsceneType) =
            instance?.setCutsceneShown(chapterId, type)
    }
}

