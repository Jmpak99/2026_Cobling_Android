package com.cobling.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

// ──────────────────────────────────────
// BlockType : 블록 종류
// ──────────────────────────────────────
enum class BlockType(val rawValue: String) {
    START("start"),
    MOVE_FORWARD("moveForward"),
    TURN_LEFT("turnLeft"),
    TURN_RIGHT("turnRight"),
    ATTACK("attack"),
    REPEAT_COUNT("repeatCount"),
    REPEAT_FOREVER("repeatForever"),
    IF("if"),
    IF_ELSE("ifElse"),
    BREAK_LOOP("breakLoop"),
    CONTINUE_LOOP("continueLoop");

    val imageName: String get() = when (this) {
        START          -> "block_start"
        MOVE_FORWARD   -> "block_move"
        TURN_LEFT      -> "block_turn_left"
        TURN_RIGHT     -> "block_turn_right"
        ATTACK         -> "block_attack"
        REPEAT_COUNT   -> "block_repeat_count"
        REPEAT_FOREVER -> "block_repeat_forever"
        IF             -> "block_if"
        IF_ELSE        -> "block_if_else"
        BREAK_LOOP     -> "block_break"
        CONTINUE_LOOP  -> "block_continue"
    }

    val isContainer: Boolean get() = when (this) {
        REPEAT_COUNT, REPEAT_FOREVER, IF, IF_ELSE -> true
        else -> false
    }

    companion object {
        fun fromRawValue(raw: String): BlockType? =
            entries.firstOrNull { it.rawValue == raw }
    }
}

// ──────────────────────────────────────
// IfCondition : if 블록 조건
// ──────────────────────────────────────
enum class IfCondition(val rawValue: String, val label: String, val isLeftSide: Boolean = true) {
    FRONT_IS_CLEAR("frontIsClear", "앞이 비어있으면"),
    FRONT_IS_BLOCKED("frontIsBlocked", "앞이 막혀있으면"),
    AT_FLAG("atFlag", "깃발에 도착했으면"),
    ENEMY_IN_FRONT("enemyInFront", "앞에 적이 있으면"),
    ALWAYS("always", "항상");

    companion object {
        fun fromRawValue(raw: String): IfCondition? =
            entries.firstOrNull { it.rawValue == raw }
    }
}

// ──────────────────────────────────────
// Block : 실제 블록 데이터 (Compose 상태 관리)
// SwiftUI의 ObservableObject + @Published → Compose mutableState
// ──────────────────────────────────────
class Block(
    val type: BlockType,
    initialValue: String? = null,
    initialCondition: IfCondition = IfCondition.FRONT_IS_CLEAR,
    initialChildren: List<Block> = emptyList(),
    initialElseChildren: List<Block> = emptyList()
) {
    val id: UUID = UUID.randomUUID()

    // 공통: 컨테이너 기본 영역 (Repeat, if(then) 블록)
    val children = mutableStateListOf<Block>().apply { addAll(initialChildren) }

    // ifElse 전용 else 영역
    val elseChildren = mutableStateListOf<Block>().apply { addAll(initialElseChildren) }

    // repeatCount 등 값
    var value: String? by mutableStateOf(initialValue)

    // if 조건값
    var condition: IfCondition by mutableStateOf(initialCondition)

    // 부모 블록 (약한 참조 역할)
    var parent: Block? = null

    init {
        for (child in children) child.parent = this
        for (child in elseChildren) child.parent = this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Block) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
