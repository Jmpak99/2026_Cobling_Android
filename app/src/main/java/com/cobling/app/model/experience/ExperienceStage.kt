package com.cobling.app.model.experience

import com.cobling.app.model.BlockType
import com.cobling.app.viewmodel.Direction

data class ExperienceStage(
    val id: String,
    val title: String,
    val description: String,
    val startX: Int,
    val startY: Int,
    val startDirection: Direction,
    val goalX: Int,
    val goalY: Int,
    val allowedBlocks: List<BlockType>,
    val rows: Int,
    val columns: Int
)

object ExperienceStageData {
    val stage1 = ExperienceStage(
        id = "experience_1",
        title = "코블링 체험",
        description = "블록을 연결해 목표 지점까지 이동해보세요.",
        startX = 1,
        startY = 3,
        startDirection = Direction.RIGHT,
        goalX = 3,
        goalY = 3,
        allowedBlocks = listOf(BlockType.MOVE_FORWARD),
        rows = 7,
        columns = 7
    )

    val grid: List<List<Int>> = listOf(
        listOf(0,0,0,0,0,0,0),
        listOf(0,0,0,0,0,0,0),
        listOf(0,0,0,0,0,0,0),
        listOf(0,0,1,1,1,0,0),
        listOf(0,0,0,0,0,0,0),
        listOf(0,0,0,0,0,0,0),
        listOf(0,0,0,0,0,0,0)
    )
}
