package com.cobling.app.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.model.Block
import com.cobling.app.ui.quest.block.BlockView
import com.cobling.app.ui.quest.block.blockImageRes
import com.cobling.app.viewmodel.DragManager
import com.cobling.app.viewmodel.DragSource
import com.cobling.app.viewmodel.QuestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.floor

// ─────────────────────────────────
// BlockPaletteView
// ─────────────────────────────────
@Composable
fun BlockPaletteView(
    viewModel: QuestViewModel,
    dragManager: DragManager
) {
    val isDeleteTarget =
        dragManager.isDragging &&
                dragManager.dragSource == DragSource.CANVAS &&
                dragManager.isOverPaletteArea()

    Box(
        modifier = Modifier
            .width(140.dp)
            .fillMaxHeight()
            .background(
                if (isDeleteTarget) Color(0xFFFFE5E5)
                else Color.White
            )
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()

                dragManager.paletteLeft = pos.x
                dragManager.paletteTop = pos.y
                dragManager.paletteRight = pos.x + coords.size.width
                dragManager.paletteBottom = pos.y + coords.size.height
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, start = 12.dp, end = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            viewModel.allowedBlocks.forEach { type ->
                val blockHeight = if (type.isContainer) 74.dp else 40.dp
                val imageWidth = if (type.isContainer) 124.dp else 120.dp
                val imageHeight = if (type.isContainer) 72.dp else 30.dp

                var itemRootX by remember(type) { mutableStateOf(0f) }
                var itemRootY by remember(type) { mutableStateOf(0f) }

                Box(
                    modifier = Modifier
                        .height(blockHeight)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInRoot()
                            itemRootX = pos.x
                            itemRootY = pos.y
                        }
                        .pointerInput(type) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val rootX = itemRootX + offset.x
                                    val rootY = itemRootY + offset.y

                                    dragManager.prepareDragging(
                                        type = type,
                                        atX = rootX,
                                        atY = rootY,
                                        block = null,
                                        parentContainer = null,
                                        source = DragSource.PALETTE
                                    )
                                },
                                onDrag = { change, _ ->
                                    val rootX = itemRootX + change.position.x
                                    val rootY = itemRootY + change.position.y

                                    dragManager.updateDragPosition(
                                        x = rootX,
                                        y = rootY
                                    )
                                },
                                onDragEnd = {
                                    dragManager.finishDrag { _, source, draggedType, _, _, isOverCanvas, insertIndex, containerTargetBlock, containerInsertIndex ->
                                        if (
                                            source == DragSource.PALETTE &&
                                            draggedType != null &&
                                            isOverCanvas
                                        ) {
                                            if (
                                                containerTargetBlock != null &&
                                                containerInsertIndex != null
                                            ) {
                                                viewModel.addBlockToContainerFromPalette(
                                                    containerBlock = containerTargetBlock,
                                                    type = draggedType,
                                                    insertIndex = containerInsertIndex
                                                )
                                            } else {
                                                viewModel.addBlockFromPalette(
                                                    type = draggedType,
                                                    insertIndex = insertIndex
                                                )
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    dragManager.cancelDrag()
                                }
                            )
                        }
                ) {
                    Icon(
                        painter = painterResource(id = blockImageRes(type)),
                        contentDescription = type.name,
                        modifier = Modifier
                            .width(120.dp)
                            .height(imageHeight),
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        if (isDeleteTarget) {
            Text(
                text = "삭제",
                color = Color(0xFFE85A5A),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            )
        }
    }
}

// ─────────────────────────────────
// BlockCanvasView
// ─────────────────────────────────
@Composable
fun BlockCanvasView(
    viewModel: QuestViewModel,
    dragManager: DragManager,
    paletteLeft: Float = 0f,
    paletteRight: Float = 0f
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val childIndent = 20.dp
    val childBlockWidth = 120.dp
    val blockSpacing = 4.dp

    var canvasViewportHeightPx by remember { mutableStateOf(0f) }

    // ─────────────────────────────────
    // 드래그 중 캔버스 위/아래 자동 스크롤
    // ─────────────────────────────────
    LaunchedEffect(dragManager.isDragging) {
        while (isActive && dragManager.isDragging) {
            val edgeThresholdPx = with(density) { 70.dp.toPx() }
            val scrollStepPx = with(density) { 8.dp.toPx() }

            val isInsideCanvasX =
                dragManager.dragX >= dragManager.canvasLeft &&
                        dragManager.dragX <= dragManager.canvasRight

            val isAroundCanvasY =
                dragManager.dragY >= dragManager.canvasTop - edgeThresholdPx &&
                        dragManager.dragY <= dragManager.canvasBottom + edgeThresholdPx

            if (isInsideCanvasX && isAroundCanvasY) {
                val distanceFromTop = dragManager.dragY - dragManager.canvasTop
                val distanceFromBottom = dragManager.canvasBottom - dragManager.dragY

                val nearTop = distanceFromTop <= edgeThresholdPx
                val nearBottom = distanceFromBottom <= edgeThresholdPx

                val topRatio = if (nearTop) {
                    ((edgeThresholdPx - distanceFromTop) / edgeThresholdPx)
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }

                val bottomRatio = if (nearBottom) {
                    ((edgeThresholdPx - distanceFromBottom) / edgeThresholdPx)
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }

                when {
                    nearTop && scrollState.value > 0 -> {
                        scrollState.scrollBy(-scrollStepPx * topRatio)
                    }

                    nearBottom && scrollState.value < scrollState.maxValue -> {
                        scrollState.scrollBy(scrollStepPx * bottomRatio)
                    }
                }
            }

            delay(24)
        }
    }

    // ─────────────────────────────────
    // 실행 중 현재 실행 블럭이 화면 밖일 때만 부드럽게 스크롤
    // ─────────────────────────────────
    LaunchedEffect(viewModel.currentExecutingBlockID) {
        val executingId = viewModel.currentExecutingBlockID ?: return@LaunchedEffect

        val flatBlocks = flattenBlocks(viewModel.startBlock.children)
        val executingIndex = flatBlocks.indexOfFirst { it.id == executingId }

        if (executingIndex == -1) return@LaunchedEffect
        if (scrollState.maxValue <= 0) return@LaunchedEffect
        if (canvasViewportHeightPx <= 0f) return@LaunchedEffect

        val topPaddingPx = with(density) { 16.dp.toPx() }
        val startBlockHeightPx = with(density) { 50.dp.toPx() }
        val estimatedBlockAreaPx = with(density) { 44.dp.toPx() }

        val targetTop =
            topPaddingPx +
                    startBlockHeightPx +
                    executingIndex * estimatedBlockAreaPx

        val targetBottom =
            targetTop + estimatedBlockAreaPx

        val visibleTop = scrollState.value.toFloat()
        val visibleBottom = visibleTop + canvasViewportHeightPx

        val marginPx = with(density) { 90.dp.toPx() }

        val targetScroll: Int? = when {
            targetTop < visibleTop + marginPx -> {
                (targetTop - marginPx).toInt()
            }

            targetBottom > visibleBottom - marginPx -> {
                (targetBottom - canvasViewportHeightPx + marginPx).toInt()
            }

            else -> {
                null
            }
        }

        if (targetScroll != null) {
            scrollState.animateScrollTo(
                targetScroll.coerceIn(0, scrollState.maxValue)
            )
        }
    }

    // ─────────────────────────────────
    // 드래그 위치에 따라 최상위 삽입 인덱스 계산
    // ─────────────────────────────────
    LaunchedEffect(
        dragManager.isDragging,
        dragManager.dragY,
        dragManager.isOverCanvas,
        dragManager.containerTargetBlock,
        scrollState.value,
        viewModel.startBlock.children.size
    ) {
        if (
            !dragManager.isDragging ||
            !dragManager.isOverCanvas ||
            dragManager.containerTargetBlock != null
        ) {
            dragManager.canvasInsertIndex = null
            return@LaunchedEffect
        }

        val topPaddingPx = with(density) { 16.dp.toPx() }
        val startBlockHeightPx = with(density) { 50.dp.toPx() }
        val normalBlockHeightPx = with(density) { 30.dp.toPx() }
        val spacingPx = with(density) { blockSpacing.toPx() }

        val childrenCount = viewModel.startBlock.children.size

        if (childrenCount == 0) {
            dragManager.canvasInsertIndex = 0
            return@LaunchedEffect
        }

        val childrenStartY =
            dragManager.canvasTop +
                    topPaddingPx +
                    startBlockHeightPx +
                    spacingPx -
                    scrollState.value

        val relativeY = dragManager.dragY - childrenStartY
        val oneBlockArea = normalBlockHeightPx + spacingPx

        val childrenEndY =
            childrenStartY + (childrenCount * oneBlockArea)

        val rawIndex = when {
            relativeY <= 0f -> {
                0
            }

            dragManager.dragY >= childrenEndY -> {
                childrenCount
            }

            else -> {
                floor(relativeY / oneBlockArea)
                    .toInt()
                    .coerceIn(0, childrenCount)
            }
        }

        dragManager.canvasInsertIndex =
            rawIndex.coerceIn(0, childrenCount)
    }

    // ─────────────────────────────────
    // 중요:
    // 바깥 Box = 보이는 캔버스 영역 좌표
    // 안쪽 Column = 실제 스크롤되는 블럭 목록
    // ─────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.1f))
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                val size = coords.size

                dragManager.canvasLeft = pos.x
                dragManager.canvasTop = pos.y
                dragManager.canvasRight = pos.x + size.width
                dragManager.canvasBottom = pos.y + size.height

                canvasViewportHeightPx = size.height.toFloat()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 10.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(blockSpacing)
        ) {
            BlockView(
                block = viewModel.startBlock,
                parentContainer = null,
                viewModel = viewModel,
                dragManager = dragManager
            )

            viewModel.startBlock.children.forEachIndexed { index, block ->
                if (
                    dragManager.isDragging &&
                    dragManager.isOverCanvas &&
                    dragManager.containerTargetBlock == null &&
                    dragManager.canvasInsertIndex == index
                ) {
                    DropIndicatorBar(
                        modifier = Modifier
                            .padding(start = childIndent)
                            .width(childBlockWidth)
                    )
                }

                Row(
                    modifier = Modifier.padding(start = childIndent)
                ) {
                    BlockView(
                        block = block,
                        parentContainer = null,
                        viewModel = viewModel,
                        dragManager = dragManager
                    )
                }
            }

            if (
                dragManager.isDragging &&
                dragManager.isOverCanvas &&
                dragManager.containerTargetBlock == null &&
                dragManager.canvasInsertIndex == viewModel.startBlock.children.size
            ) {
                DropIndicatorBar(
                    modifier = Modifier
                        .padding(start = childIndent)
                        .width(childBlockWidth)
                )
            }

            Spacer(
                modifier = Modifier.height(240.dp)
            )
        }
    }
}

// ─────────────────────────────────
// 실행 순서 기준으로 블럭 펼치기
// ─────────────────────────────────
private fun flattenBlocks(blocks: List<Block>): List<Block> {
    val result = mutableListOf<Block>()

    fun dfs(list: List<Block>) {
        list.forEach { block ->
            result.add(block)

            if (block.type.isContainer) {
                dfs(block.children)
            }
        }
    }

    dfs(blocks)
    return result
}

// ─────────────────────────────────
// DropIndicatorBar
// ─────────────────────────────────
@Composable
fun DropIndicatorBar(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF58ED98))
    )
}