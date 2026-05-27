package com.cobling.app.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

// ──────────────────────────────────────
// Firestore 퀘스트 데이터 모델
// ──────────────────────────────────────
data class SubQuestDocument(
    @DocumentId val id: String? = null,
    val title: String = "",
    val description: String = "",
    val objective: String = "",
    val order: Int = 0,
    val isActive: Boolean = false,
    val preId: String? = null,
    val story: StoryData? = null,
    val hint: HintData? = null,
    val rewards: Rewards = Rewards(),
    val rules: Rules = Rules(),
    val map: MapData = MapData()
)

data class MapData(
    val goal: Position = Position(),
    val start: Position = Position(),
    val grid: List<String> = emptyList(),     // "0,1,0,0" 형태 문자열 배열
    val size: MapSize = MapSize(),
    val startDirection: String = "right",
    val legend: Legend = Legend(),
    val enemies: List<Enemy>? = null
) {
    // grid(List<String>) → List<List<Int>> 변환
    val parsedGrid: List<List<Int>> get() = grid.map { rowString ->
        rowString.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}

data class Enemy(
    val id: String = "",
    val row: Int = 0,
    val col: Int = 0
) {
    val position: Position get() = Position(row = row, col = col)
}

data class Position(
    val row: Int = 0,
    val col: Int = 0
)

data class MapSize(
    val rows: Int = 0,
    val cols: Int = 0
)

data class Legend(
    val empty: Int = 0,
    val path: Int = 1,
    val start: Int = 2,
    val goal: Int = 3
)

data class Rewards(
    val baseExp: Int = 0,
    val perfectBonusExp: Int = 0
)

data class Rules(
    val allowBlocks: List<String> = emptyList(),
    val attackRange: Int = 0,
    val maxSteps: Int = 0,
    val allowedIfConditions: List<String>? = null,
    val defaultIfCondition: String? = null
)

data class StoryData(
    var message: String = "",

    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = false
)

data class HintData(
    var message: String = "",

    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = false
)

// ──────────────────────────────────────
// QuestList용 모델
// ──────────────────────────────────────
data class QuestDocument(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val order: Int = 0,
    val recommendedLevel: Int = 1,
    val isActive: Boolean = false,
    val allowedBlocks: List<String>? = null
)

enum class QuestStatus { COMPLETED, IN_PROGRESS, LOCKED }

// ──────────────────────────────────────
// SubQuest (QuestDetail용 뷰 모델)
// ──────────────────────────────────────
enum class SubQuestState { COMPLETED, IN_PROGRESS, LOCKED }

data class SubQuest(
    val id: String,
    val title: String,
    val description: String,
    val state: SubQuestState,
    val perfectClear: Boolean,
    val order: Int
)
