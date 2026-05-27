package com.cobling.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.cobling.app.model.Block
import com.cobling.app.model.BlockType
import java.util.UUID

enum class DragSource {
    PALETTE,
    CANVAS
}

class DragManager {

    // ─────────────────────────────────
    // Dragging State
    // ─────────────────────────────────
    var isDragging: Boolean by mutableStateOf(false)

    var draggingType: BlockType? by mutableStateOf(null)
    var draggingBlock: Block? by mutableStateOf(null)
    var draggingBlockID: UUID? by mutableStateOf(null)
    var draggingParentContainer: Block? by mutableStateOf(null)

    var dragSource: DragSource by mutableStateOf(DragSource.PALETTE)

    // Root 기준 좌표
    var dragPosition: Offset by mutableStateOf(Offset.Zero)
    var dragStartOffset: Offset by mutableStateOf(Offset.Zero)

    var dragX: Float by mutableStateOf(0f)
    var dragY: Float by mutableStateOf(0f)

    // ─────────────────────────────────
    // Canvas Bounds - Root 기준 좌표
    // ─────────────────────────────────
    var canvasLeft: Float = 0f
    var canvasTop: Float = 0f
    var canvasRight: Float = 0f
    var canvasBottom: Float = 0f

    // ─────────────────────────────────
    // Palette Bounds - Root 기준 좌표
    // ─────────────────────────────────
    var paletteLeft: Float = 0f
    var paletteTop: Float = 0f
    var paletteRight: Float = 0f
    var paletteBottom: Float = 0f

    // ─────────────────────────────────
    // Hover / Drop Target State
    // ─────────────────────────────────
    var isOverCanvas: Boolean by mutableStateOf(false)
    var canvasInsertIndex: Int? by mutableStateOf(null)

    var isOverContainer: Boolean by mutableStateOf(false)
    var containerFrame: Rect? by mutableStateOf(null)
    var containerTargetBlock: Block? by mutableStateOf(null)
    var containerInsertIndex: Int? by mutableStateOf(null)

    // ─────────────────────────────────
    // Drag Start
    // ─────────────────────────────────
    fun prepareDragging(
        type: BlockType,
        atX: Float,
        atY: Float,
        block: Block? = null,
        parentContainer: Block? = null,
        source: DragSource
    ) {
        if (isDragging) return

        draggingType = type
        draggingBlock = block
        draggingBlockID = block?.id
        draggingParentContainer = parentContainer
        dragSource = source

        dragX = atX
        dragY = atY
        dragPosition = Offset(atX, atY)
        dragStartOffset = Offset(atX, atY)

        isDragging = true
        updateHoverState(atX, atY)
    }

    fun prepareDragging(
        type: BlockType,
        at: Offset,
        offset: Offset = Offset.Zero,
        block: Block? = null,
        parentContainer: Block? = null,
        source: DragSource
    ) {
        prepareDragging(
            type = type,
            atX = at.x,
            atY = at.y,
            block = block,
            parentContainer = parentContainer,
            source = source
        )
    }

    // ─────────────────────────────────
    // Drag Move
    // ─────────────────────────────────
    fun updateDragPosition(x: Float, y: Float) {
        if (!isDragging) return

        dragX = x
        dragY = y
        dragPosition = Offset(x, y)

        updateHoverState(x, y)
    }

    fun updateDragPosition(position: Offset) {
        updateDragPosition(position.x, position.y)
    }

    private fun updateHoverState(x: Float, y: Float) {
        val overPalette =
            x >= paletteLeft &&
                    x <= paletteRight &&
                    y >= paletteTop &&
                    y <= paletteBottom

        val overCanvas =
            x >= canvasLeft &&
                    x <= canvasRight &&
                    y >= canvasTop &&
                    y <= canvasBottom

        isOverCanvas = overCanvas && !overPalette

        if (!isOverCanvas) {
            canvasInsertIndex = null
            isOverContainer = false
            containerTargetBlock = null
            containerInsertIndex = null
        }
    }

    // ─────────────────────────────────
    // Drag End
    // ─────────────────────────────────
    fun finishDrag(
        onFinish: (
            endPosition: Offset,
            source: DragSource,
            type: BlockType?,
            block: Block?,
            parentContainer: Block?,
            isOverCanvas: Boolean,
            canvasInsertIndex: Int?,
            containerTargetBlock: Block?,
            containerInsertIndex: Int?
        ) -> Unit
    ) {
        if (!isDragging) return

        val endPosition = Offset(dragX, dragY)

        onFinish(
            endPosition,
            dragSource,
            draggingType,
            draggingBlock,
            draggingParentContainer,
            isOverCanvas,
            canvasInsertIndex,
            containerTargetBlock,
            containerInsertIndex
        )

        reset()
    }

    fun cancelDrag() {
        reset()
    }

    fun isOverPaletteArea(): Boolean {
        return dragX >= paletteLeft &&
                dragX <= paletteRight &&
                dragY >= paletteTop &&
                dragY <= paletteBottom
    }

    fun setContainerTarget(
        block: Block,
        insertIndex: Int,
        frame: Rect? = null
    ) {
        isOverContainer = true
        containerTargetBlock = block
        containerInsertIndex = insertIndex
        containerFrame = frame

        // 컨테이너 내부가 타겟이면 최상위 캔버스 삽입 인덱스는 꺼야 함
        canvasInsertIndex = null
    }

    fun clearContainerTarget(block: Block? = null) {
        if (block == null || containerTargetBlock?.id == block.id) {
            isOverContainer = false
            containerTargetBlock = null
            containerInsertIndex = null
            containerFrame = null
        }
    }

    // ─────────────────────────────────
    // Reset
    // ─────────────────────────────────
    fun reset() {
        isDragging = false

        draggingType = null
        draggingBlock = null
        draggingBlockID = null
        draggingParentContainer = null

        dragSource = DragSource.PALETTE

        dragPosition = Offset.Zero
        dragStartOffset = Offset.Zero
        dragX = 0f
        dragY = 0f

        isOverCanvas = false
        canvasInsertIndex = null

        isOverContainer = false
        containerFrame = null
        containerTargetBlock = null
        containerInsertIndex = null
    }
}