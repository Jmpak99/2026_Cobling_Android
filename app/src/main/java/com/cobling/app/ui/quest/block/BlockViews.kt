package com.cobling.app.ui.quest.block

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.cobling.app.model.Block
import com.cobling.app.model.BlockType
import com.cobling.app.model.IfCondition
import com.cobling.app.ui.quest.DropIndicatorBar
import com.cobling.app.viewmodel.DragManager
import com.cobling.app.viewmodel.DragSource
import com.cobling.app.viewmodel.QuestViewModel
import kotlin.math.floor

@Composable
fun BlockView(
    block: Block,
    parentContainer: Block?,
    viewModel: QuestViewModel,
    dragManager: DragManager
) {
    if (block.type.isContainer) {
        ContainerBlockView(
            block = block,
            viewModel = viewModel,
            dragManager = dragManager
        )
    } else {
        NormalBlockView(
            block = block,
            parentContainer = parentContainer,
            viewModel = viewModel,
            dragManager = dragManager
        )
    }
}

@Composable
fun NormalBlockView(
    block: Block,
    parentContainer: Block?,
    viewModel: QuestViewModel,
    dragManager: DragManager
) {
    if (block.type == BlockType.START) {
        Icon(
            painter = painterResource(id = blockImageRes(block.type)),
            contentDescription = block.type.name,
            modifier = Modifier.size(160.dp, 50.dp),
            tint = Color.Unspecified
        )
        return
    }

    var blockRootX by remember { mutableStateOf(0f) }
    var blockRootY by remember { mutableStateOf(0f) }

    val isExecuting = viewModel.currentExecutingBlockID == block.id

    val opacity = when {
        dragManager.draggingBlockID == block.id -> 0.25f
        viewModel.isExecuting && !isExecuting -> 0.3f
        else -> 1f
    }

    Icon(
        painter = painterResource(id = blockImageRes(block.type)),
        contentDescription = block.type.name,
        modifier = Modifier
            .size(120.dp, 30.dp)
            .alpha(opacity)
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                blockRootX = pos.x
                blockRootY = pos.y
            }
            .pointerInput(block.id) {
                if (viewModel.isExecuting) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        dragManager.prepareDragging(
                            type = block.type,
                            atX = blockRootX + offset.x,
                            atY = blockRootY + offset.y,
                            block = block,
                            parentContainer = parentContainer,
                            source = DragSource.CANVAS
                        )
                    },
                    onDrag = { change, _ ->
                        dragManager.updateDragPosition(
                            x = blockRootX + change.position.x,
                            y = blockRootY + change.position.y
                        )
                    },
                    onDragEnd = {
                        dragManager.finishDrag { _, source, _, draggedBlock, oldParentContainer, isOverCanvas, insertIndex, containerTargetBlock, containerInsertIndex ->

                            if (
                                source == DragSource.CANVAS &&
                                draggedBlock != null &&
                                dragManager.isOverPaletteArea()
                            ) {
                                viewModel.removeBlockFromCanvas(
                                    block = draggedBlock,
                                    parentContainer = oldParentContainer
                                )
                                return@finishDrag
                            }

                            if (
                                source == DragSource.CANVAS &&
                                draggedBlock != null &&
                                isOverCanvas
                            ) {
                                if (
                                    containerTargetBlock != null &&
                                    containerInsertIndex != null
                                ) {
                                    viewModel.moveBlockToContainer(
                                        movingBlock = draggedBlock,
                                        oldParent = oldParentContainer,
                                        newParent = containerTargetBlock,
                                        insertIndex = containerInsertIndex
                                    )
                                } else {
                                    viewModel.moveBlockToRoot(
                                        movingBlock = draggedBlock,
                                        oldParent = oldParentContainer,
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
            },
        tint = Color.Unspecified
    )
}

@Composable
fun ContainerBlockView(
    block: Block,
    viewModel: QuestViewModel,
    dragManager: DragManager
) {
    val density = LocalDensity.current

    var blockRootX by remember { mutableStateOf(0f) }
    var blockRootY by remember { mutableStateOf(0f) }

    var innerLeft by remember { mutableStateOf(0f) }
    var innerTop by remember { mutableStateOf(0f) }
    var innerRight by remember { mutableStateOf(0f) }
    var innerBottom by remember { mutableStateOf(0f) }

    val containerTint = when (block.type) {
        BlockType.REPEAT_COUNT,
        BlockType.REPEAT_FOREVER -> Color(0xFF86B0FF)

        BlockType.IF,
        BlockType.IF_ELSE -> Color(0xFF4CCB7A)

        else -> Color(0xFF86B0FF)
    }

    val emptyGuide = when (block.type) {
        BlockType.REPEAT_COUNT,
        BlockType.REPEAT_FOREVER -> "여기에 블록을 넣어주세요"
        else -> "조건이 맞으면 실행할 블록을 넣어주세요"
    }

    val isExecuting = viewModel.currentExecutingBlockID == block.id

    val containerVisualOpacity = when {
        viewModel.isExecuting && !isExecuting -> 0.3f
        else -> 1f
    }

    val wholeBlockDragOpacity = when {
        dragManager.draggingBlockID == block.id -> 0.25f
        else -> 1f
    }

    LaunchedEffect(
        dragManager.isDragging,
        dragManager.dragX,
        dragManager.dragY,
        dragManager.isOverCanvas,
        innerLeft,
        innerTop,
        innerRight,
        innerBottom,
        block.children.size
    ) {
        if (!dragManager.isDragging || !dragManager.isOverCanvas) {
            if (dragManager.containerTargetBlock?.id == block.id) {
                dragManager.isOverContainer = false
                dragManager.containerTargetBlock = null
                dragManager.containerInsertIndex = null
                dragManager.containerFrame = null
            }
            return@LaunchedEffect
        }

        val isInside =
            dragManager.dragX in innerLeft..innerRight &&
                    dragManager.dragY in innerTop..innerBottom

        if (isInside) {
            val normalBlockHeightPx = with(density) { 30.dp.toPx() }
            val spacingPx = with(density) { 6.dp.toPx() }

            val relativeY = dragManager.dragY - innerTop

            val rawIndex = if (relativeY <= 0f) {
                0
            } else {
                floor(relativeY / (normalBlockHeightPx + spacingPx)).toInt()
            }

            val safeIndex = rawIndex.coerceIn(0, block.children.size)

            dragManager.isOverContainer = true
            dragManager.containerTargetBlock = block
            dragManager.containerInsertIndex = safeIndex
            dragManager.containerFrame = Rect(
                left = innerLeft,
                top = innerTop,
                right = innerRight,
                bottom = innerBottom
            )

            dragManager.canvasInsertIndex = null
        } else {
            if (dragManager.containerTargetBlock?.id == block.id) {
                dragManager.isOverContainer = false
                dragManager.containerTargetBlock = null
                dragManager.containerInsertIndex = null
                dragManager.containerFrame = null
            }
        }
    }

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .padding(bottom = 2.dp)
            .alpha(wholeBlockDragOpacity)
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                blockRootX = pos.x
                blockRootY = pos.y
            }
            .pointerInput(block.id) {
                if (viewModel.isExecuting) return@pointerInput

                detectDragGestures(
                    onDragStart = { offset ->
                        dragManager.prepareDragging(
                            type = block.type,
                            atX = blockRootX + offset.x,
                            atY = blockRootY + offset.y,
                            block = block,
                            parentContainer = viewModel.findParentContainer(block),
                            source = DragSource.CANVAS
                        )
                    },
                    onDrag = { change, _ ->
                        dragManager.updateDragPosition(
                            x = blockRootX + change.position.x,
                            y = blockRootY + change.position.y
                        )
                    },
                    onDragEnd = {
                        dragManager.finishDrag { _, source, _, draggedBlock, oldParentContainer, isOverCanvas, insertIndex, containerTargetBlock, containerInsertIndex ->

                            if (
                                source == DragSource.CANVAS &&
                                draggedBlock != null &&
                                dragManager.isOverPaletteArea()
                            ) {
                                viewModel.removeBlockFromCanvas(
                                    block = draggedBlock,
                                    parentContainer = oldParentContainer
                                )
                                return@finishDrag
                            }

                            if (
                                source == DragSource.CANVAS &&
                                draggedBlock != null &&
                                isOverCanvas
                            ) {
                                if (
                                    containerTargetBlock != null &&
                                    containerInsertIndex != null
                                ) {
                                    viewModel.moveBlockToContainer(
                                        movingBlock = draggedBlock,
                                        oldParent = oldParentContainer,
                                        newParent = containerTargetBlock,
                                        insertIndex = containerInsertIndex
                                    )
                                } else {
                                    viewModel.moveBlockToRoot(
                                        movingBlock = draggedBlock,
                                        oldParent = oldParentContainer,
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
        Box(
            modifier = Modifier
                .width(12.dp)
                .fillMaxHeight()
                .alpha(containerVisualOpacity)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(containerTint)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(165.dp)
                    .height(36.dp)
                    .alpha(containerVisualOpacity)
                    .clip(RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp))
                    .background(containerTint)
            ) {
                when (block.type) {
                    BlockType.REPEAT_COUNT,
                    BlockType.REPEAT_FOREVER -> {
                        RepeatHeaderView(
                            block = block,
                            containerTint = containerTint
                        )
                    }

                    BlockType.IF,
                    BlockType.IF_ELSE -> {
                        IfHeaderView(
                            block = block,
                            options = viewModel.currentAllowedIfConditions,
                            defaultCondition = viewModel.currentDefaultIfCondition,
                            containerTint = containerTint
                        )
                    }

                    else -> {
                        RepeatHeaderView(
                            block = block,
                            containerTint = containerTint
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInRoot()

                        innerLeft = pos.x
                        innerTop = pos.y
                        innerRight = pos.x + coords.size.width
                        innerBottom = pos.y + coords.size.height
                    },
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (block.children.isEmpty()) {
                    Text(
                        text = emptyGuide,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = containerTint.copy(alpha = 0.35f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (
                        dragManager.isDragging &&
                        dragManager.isOverCanvas &&
                        dragManager.containerTargetBlock?.id == block.id &&
                        dragManager.containerInsertIndex == 0
                    ) {
                        DropIndicatorBar(
                            modifier = Modifier.width(120.dp)
                        )
                    }
                }

                block.children.forEachIndexed { index, child ->
                    if (
                        dragManager.isDragging &&
                        dragManager.isOverCanvas &&
                        dragManager.containerTargetBlock?.id == block.id &&
                        dragManager.containerInsertIndex == index
                    ) {
                        DropIndicatorBar(
                            modifier = Modifier.width(120.dp)
                        )
                    }

                    BlockView(
                        block = child,
                        parentContainer = block,
                        viewModel = viewModel,
                        dragManager = dragManager
                    )
                }

                if (
                    block.children.isNotEmpty() &&
                    dragManager.isDragging &&
                    dragManager.isOverCanvas &&
                    dragManager.containerTargetBlock?.id == block.id &&
                    dragManager.containerInsertIndex == block.children.size
                ) {
                    DropIndicatorBar(
                        modifier = Modifier.width(120.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(12.dp)
                    .alpha(containerVisualOpacity)
                    .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                    .background(containerTint)
            )
        }
    }
}

@Composable
fun RepeatHeaderView(
    block: Block,
    containerTint: Color
) {
    val options = (1..10).toList()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box {
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .clickable { expanded = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${block.value ?: "1"} ▼",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { count ->
                    DropdownMenuItem(
                        text = { Text("$count") },
                        onClick = {
                            block.value = "$count"
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "번 반복하기",
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "↻",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = containerTint
            )
        }
    }
}

@Composable
fun IfHeaderView(
    block: Block,
    options: List<IfCondition>,
    defaultCondition: IfCondition,
    containerTint: Color
) {
    LaunchedEffect(options, defaultCondition) {
        if (block.type == BlockType.IF || block.type == BlockType.IF_ELSE) {
            if (!options.contains(block.condition)) {
                block.condition = defaultCondition
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var expanded by remember { mutableStateOf(false) }

        Box {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .clickable { expanded = true }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${block.condition.label} ▼",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { cond ->
                    DropdownMenuItem(
                        text = { Text(cond.label) },
                        onClick = {
                            block.condition = cond
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = containerTint
            )
        }
    }
}

fun blockImageRes(type: BlockType): Int = when (type) {
    BlockType.START -> R.drawable.block_start
    BlockType.MOVE_FORWARD -> R.drawable.block_move
    BlockType.TURN_LEFT -> R.drawable.block_turn_left
    BlockType.TURN_RIGHT -> R.drawable.block_turn_right
    BlockType.ATTACK -> R.drawable.block_attack
    BlockType.REPEAT_COUNT -> R.drawable.block_repeat_count
    BlockType.REPEAT_FOREVER -> R.drawable.block_repeat_forever
    BlockType.IF -> R.drawable.block_if
    BlockType.IF_ELSE -> R.drawable.block_if_else
    BlockType.BREAK_LOOP -> R.drawable.block_break
    BlockType.CONTINUE_LOOP -> R.drawable.block_continue
}