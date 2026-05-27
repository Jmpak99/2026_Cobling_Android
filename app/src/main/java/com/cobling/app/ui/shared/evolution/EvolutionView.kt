package com.cobling.app.ui.shared.evolution

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

private enum class EvolutionStage(val assetSuffix: String) {
    EGG("egg"), KID("kid"), COBLING("cobling"), LEGEND("legend");
    val assetName get() = "cobling_stage_$assetSuffix"
}

private fun fromToStage(level: Int): Pair<EvolutionStage, EvolutionStage> = when (level) {
    5  -> Pair(EvolutionStage.EGG, EvolutionStage.KID)
    10 -> Pair(EvolutionStage.KID, EvolutionStage.COBLING)
    15 -> Pair(EvolutionStage.COBLING, EvolutionStage.LEGEND)
    else -> Pair(EvolutionStage.EGG, EvolutionStage.KID)
}

private fun evolutionTexts(level: Int): Triple<String, String, String> = when (level) {
    5  -> Triple("Lv 5 달성!", "코블링이 한 단계 성장했어요.", "\u201c이제는 멈추고 생각할 수 있어.\u201d")
    10 -> Triple("Lv 10 달성!", "코블링의 형태가 완성됐어요.", "\u201c내 안에 흐름이 보이기 시작해!\u201d")
    15 -> Triple("Lv 15 달성!", "전설의 코블링이 깨어났어요.", "\u201c이 숲의 규칙… 내가 다시 쓸게.\u201d")
    else -> Triple("진화!", "코블링이 변화하고 있어요.", "\u201c더 강해졌어!\u201d")
}

@Composable
fun EvolutionView(
    reachedLevel: Int,
    onCompleted: () -> Unit
) {
    val (fromStage, toStage) = fromToStage(reachedLevel)
    val (title, subtitle, quote) = evolutionTexts(reachedLevel)

    var swapDone by remember { mutableStateOf(false) }
    var glowOpacity by remember { mutableStateOf(0f) }
    var flashOpacity by remember { mutableStateOf(0f) }
    var canTapComplete by remember { mutableStateOf(false) }
    var isFinishing by remember { mutableStateOf(false) }

    val glowScale by animateFloatAsState(
        targetValue = glowOpacity,
        animationSpec = tween(750),
        label = "glow"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (isFinishing) 0.95f else 1f,
        animationSpec = tween(250),
        label = "card"
    )

    LaunchedEffect(Unit) {
        glowOpacity = 1f
        delay(850)
        flashOpacity = 0.92f
        delay(520)
        swapDone = true
        delay(1300)
        flashOpacity = 0f
        glowOpacity = 0f
        delay(500)
        canTapComplete = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .scale(cardScale)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(subtitle, fontSize = 14.sp, color = Color(0xFF333333))

            Box(contentAlignment = Alignment.Center) {
                // 글로우 원
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD475).copy(alpha = 0.32f))
                )
                // 캐릭터
                val stageToShow = if (!swapDone) fromStage else toStage
                val drawableId = stageDrawableId(stageToShow.assetName)
                Icon(
                    painter = painterResource(id = drawableId),
                    contentDescription = "코블링",
                    modifier = Modifier.size(if (swapDone) 160.dp else 140.dp),
                    tint = Color.Unspecified
                )
                // 플래시
                if (flashOpacity > 0f) {
                    Box(
                        modifier = Modifier
                            .size(270.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF7D6).copy(alpha = flashOpacity))
                    )
                }
            }

            Text(quote, fontSize = 14.sp, color = Color(0xFF444444))

            Button(
                onClick = {
                    if (!isFinishing && canTapComplete) {
                        isFinishing = true
                        // Firestore 업데이트 후 완료 콜백
                        onCompleted()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = canTapComplete && !isFinishing,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD475))
            ) {
                Text(
                    text = if (isFinishing) "처리 중..." else "진화 완료",
                    color = Color.Black
                )
            }
        }
    }
}

private fun stageDrawableId(assetName: String): Int {
    return when (assetName) {
        "cobling_stage_egg"    -> R.drawable.cobling_stage_egg
        "cobling_stage_kid"    -> R.drawable.cobling_stage_kid
        "cobling_stage_cobling"-> R.drawable.cobling_stage_cobling
        "cobling_stage_legend" -> R.drawable.cobling_stage_legend
        else -> R.drawable.cobling_stage_egg
    }
}

suspend fun markEvolutionAsCompletedOnServer() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    try {
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .update(mapOf("character.evolutionPending" to false))
            .await()
    } catch (e: Exception) {
        android.util.Log.e("EvolutionView", "Evolution update failed: ${e.message}")
    }
}
