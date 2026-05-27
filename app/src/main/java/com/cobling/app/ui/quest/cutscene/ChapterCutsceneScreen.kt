package com.cobling.app.ui.quest.cutscene

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.cobling.app.model.ChapterCutscene
import com.cobling.app.model.DialogueSpeaker
import com.cobling.app.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun ChapterCutsceneScreen(
    chapterId: String,
    cutscene: ChapterCutscene,
    authViewModel: AuthViewModel,
    onClose: () -> Unit
) {
    var index by remember { mutableStateOf(0) }
    var displayedText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    var bgOpacity by remember { mutableStateOf(0f) }
    var charOpacity by remember { mutableStateOf(0f) }

    var liveCharacterStage by remember {
        mutableStateOf<String?>(null)
    }

    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()

    val currentLine = cutscene.lines[index.coerceIn(0, cutscene.lines.lastIndex)]
    val isLast = index >= cutscene.lines.lastIndex

    val backgroundId = backgroundDrawableIdByChapter(chapterId)

    DisposableEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            onDispose { }
        } else {
            val listener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, _ ->
                    val stageFromDotPath = snapshot?.getString("character.stage")

                    val stageFromMap = (snapshot?.get("character") as? Map<*, *>)
                        ?.get("stage") as? String

                    liveCharacterStage = stageFromDotPath ?: stageFromMap
                }

            onDispose {
                listener.remove()
            }
        }
    }

    val profileStage = userProfile
        ?.character
        ?.stage
        ?.trim()
        ?.lowercase()

    val firestoreStage = liveCharacterStage
        ?.trim()
        ?.lowercase()

    val currentStage = firestoreStage
        ?: profileStage
        ?: "egg"

    val safeStage = if (currentStage in setOf("egg", "kid", "cobling", "legend")) {
        currentStage
    } else {
        "egg"
    }

    val coblingId = stageDrawableIdByStage(safeStage)
    val spiritId = R.drawable.spirit_forest

    LaunchedEffect(Unit) {
        bgOpacity = 1f
        delay(120)
        charOpacity = 1f
    }

    LaunchedEffect(index) {
        isTyping = true
        displayedText = ""

        delay(40)

        for (ch in currentLine.text) {
            displayedText += ch
            delay(28)
        }

        isTyping = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                if (isTyping) {
                    displayedText = currentLine.text
                    isTyping = false
                } else if (!isLast) {
                    index++
                }
            }
    ) {
        Image(
            painter = painterResource(id = backgroundId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(bgOpacity),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f * bgOpacity))
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 300.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isCobling = currentLine.speaker == DialogueSpeaker.COBLING
            val isSpirit = currentLine.speaker == DialogueSpeaker.SPIRIT

            Image(
                painter = painterResource(id = coblingId),
                contentDescription = "코블링",
                modifier = Modifier
                    .height(140.dp)
                    .weight(1f)
                    .scale(if (isCobling) 1.03f else 0.97f)
                    .alpha(
                        if (isCobling) {
                            charOpacity
                        } else {
                            0.55f * charOpacity
                        }
                    ),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(12.dp))

            Image(
                painter = painterResource(id = spiritId),
                contentDescription = "정령",
                modifier = Modifier
                    .height(140.dp)
                    .weight(1f)
                    .scale(if (isSpirit) 1.03f else 0.97f)
                    .alpha(
                        if (isSpirit) {
                            charOpacity
                        } else {
                            0.55f * charOpacity
                        }
                    ),
                contentScale = ContentScale.Fit
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .padding(bottom = 130.dp)
        ) {
            CutsceneDialogueBox(
                speakerName = currentLine.speaker.displayName,
                text = displayedText,
                isLast = isLast,
                primaryButtonTitle = cutscene.type.primaryButtonTitle,
                isTyping = isTyping,
                onNext = {
                    if (isTyping) {
                        displayedText = currentLine.text
                        isTyping = false
                    } else if (!isLast) {
                        index++
                    }
                },
                onPrimary = onClose
            )
        }
    }
}

@Composable
private fun CutsceneDialogueBox(
    speakerName: String,
    text: String,
    isLast: Boolean,
    primaryButtonTitle: String,
    isTyping: Boolean,
    onNext: () -> Unit,
    onPrimary: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Black.copy(alpha = 0.55f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = speakerName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = text,
            fontSize = 18.sp,
            color = Color.White,
            lineHeight = 26.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (isLast) {
                Button(
                    onClick = onPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(pulse),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.92f)
                    )
                ) {
                    Text(
                        text = primaryButtonTitle,
                        color = Color.Black.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("✨")
                }
            } else {
                TextButton(
                    onClick = onNext,
                    modifier = Modifier.background(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    )
                ) {
                    Text(
                        text = if (isTyping) "스킵 ⏩" else "다음 ›",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun backgroundDrawableIdByChapter(chapterId: String): Int {
    val chapterNumber = chapterId
        .filter { it.isDigit() }
        .toIntOrNull() ?: 1

    return when (chapterNumber) {
        1 -> R.drawable.bg_ch1_intro
        2 -> R.drawable.bg_ch2_intro
        3 -> R.drawable.bg_ch3_intro
        4 -> R.drawable.bg_ch4_intro
        5 -> R.drawable.bg_ch5_intro
        else -> R.drawable.bg_ch1_intro
    }
}

private fun stageDrawableIdByStage(stage: String): Int {
    return when (stage) {
        "egg" -> R.drawable.cobling_stage_egg
        "kid" -> R.drawable.cobling_stage_kid
        "cobling" -> R.drawable.cobling_stage_cobling
        "legend" -> R.drawable.cobling_stage_legend
        else -> R.drawable.cobling_stage_egg
    }
}