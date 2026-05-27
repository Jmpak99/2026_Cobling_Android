package com.cobling.app.ui.quest.block

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cobling.app.viewmodel.DragManager
import kotlin.math.roundToInt

@Composable
fun GhostBlockOverlay(
    dragManager: DragManager
) {
    if (!dragManager.isDragging) return

    val type = dragManager.draggingType ?: return

    var rootX by remember { mutableFloatStateOf(0f) }
    var rootY by remember { mutableFloatStateOf(0f) }

    val ghostWidth = if (type.isContainer) 165.dp else 120.dp
    val ghostHeight = if (type.isContainer) 60.dp else 30.dp

    val xOffset = if (type.isContainer) 82f else 60f
    val yOffset = if (type.isContainer) 30f else 15f

    Box(
        modifier = Modifier
            .zIndex(999f)
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                rootX = pos.x
                rootY = pos.y
            }
            .offset {
                IntOffset(
                    x = (dragManager.dragX - rootX - xOffset).roundToInt(),
                    y = (dragManager.dragY - rootY - yOffset).roundToInt()
                )
            }
    ) {
        Icon(
            painter = painterResource(id = blockImageRes(type)),
            contentDescription = type.name,
            modifier = Modifier
                .width(ghostWidth)
                .height(ghostHeight)
                .scale(1.05f)
                .alpha(0.75f),
            tint = Color.Unspecified
        )
    }
}