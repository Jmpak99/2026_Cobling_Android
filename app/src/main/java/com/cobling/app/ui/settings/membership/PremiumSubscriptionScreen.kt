package com.cobling.app.ui.settings.membership

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cobling.app.viewmodel.AuthViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private enum class PremiumPlan(val firestoreValue: String, val label: String) {
    LIFETIME("lifetime", "평생 이용권"),
    MONTHLY("monthly", "월간 구독");

    companion object {
        fun fromFirestore(raw: String?): PremiumPlan? {
            return when (raw?.trim()?.lowercase()) {
                "lifetime", "lifetime_one_time", "permanent" -> LIFETIME
                "monthly", "month" -> MONTHLY
                else -> null
            }
        }
    }
}

@Composable
fun PremiumSubscriptionScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val coblingGold = Color(0xFFFFD27B)
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()

    val dbPremiumActive = userProfile?.premium?.isActive ?: false
    val dbCurrentPlan   = PremiumPlan.fromFirestore(userProfile?.premium?.plan)
    val isLifetimeUser  = dbPremiumActive && dbCurrentPlan == PremiumPlan.LIFETIME

    var selectedPlan by remember { mutableStateOf(PremiumPlan.LIFETIME) }

    LaunchedEffect(userProfile) {
        PremiumPlan.fromFirestore(userProfile?.premium?.plan)?.let { selectedPlan = it }
    }

    val isCurrentPlan = dbPremiumActive && selectedPlan == dbCurrentPlan
    val buttonTitle = when {
        isCurrentPlan -> "현재 이용 중인 플랜입니다"
        isLifetimeUser && selectedPlan == PremiumPlan.MONTHLY -> "평생 이용 중인 계정입니다"
        selectedPlan == PremiumPlan.LIFETIME -> "구매하기 - ₩29,000"
        else -> "구독하기 - ₩3,300"
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 헤더
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "뒤로", modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.weight(1f))
            Text("프리미엄 구독", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("👑", fontSize = 48.sp, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text("프리미엄 멤버십", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("특별한 혜택을 누리세요", fontSize = 16.sp, color = Color.Gray)
            }

            // 혜택 카드
            BenefitCard(icon = Icons.Filled.Block,      title = "광고 제거",           subtitle = "플레이 중 나오는 광고가 제거됩니다")
            BenefitCard(icon = Icons.Filled.Star,       title = "EXP +5% 보너스",      subtitle = "클리어 경험치에 5% 보너스가 적용돼요")
            BenefitCard(icon = Icons.Filled.LibraryBooks, title = "추가 챕터 (시즌 2-3)", subtitle = "프리미엄 전용 시즌 콘텐츠를 이용할 수 있어요")

            // 플랜 카드
            PlanCard(
                title = "평생 이용권", subtitle = "1회 결제",
                priceText = "₩29,000", unitText = "/영구적",
                highlightText = "한 번 결제로 영구 이용!",
                isSelected = selectedPlan == PremiumPlan.LIFETIME,
                showCurrentBadge = dbPremiumActive && dbCurrentPlan == PremiumPlan.LIFETIME,
                badgeColor = coblingGold,
                onClick = { selectedPlan = PremiumPlan.LIFETIME }
            )
            PlanCard(
                title = "월간 구독", subtitle = "1개월마다 결제",
                priceText = "₩3,300", unitText = "/월",
                highlightText = if (isLifetimeUser) "평생 이용 중인 계정은 월간 구독을 이용할 수 없어요" else null,
                isSelected = selectedPlan == PremiumPlan.MONTHLY,
                showCurrentBadge = dbPremiumActive && dbCurrentPlan == PremiumPlan.MONTHLY,
                badgeColor = coblingGold,
                onClick = { if (!isLifetimeUser) selectedPlan = PremiumPlan.MONTHLY },
                dimmed = isLifetimeUser
            )

            // 구매 버튼
            Button(
                onClick = {
                    if (!isCurrentPlan && !(isLifetimeUser && selectedPlan == PremiumPlan.MONTHLY)) {
                        scope.launch {
                            setPremiumInFirestore(authViewModel.currentUserId, selectedPlan)
                            authViewModel.refreshUserProfileIfNeeded()
                        }
                    }
                },
                enabled = !isCurrentPlan && !(isLifetimeUser && selectedPlan == PremiumPlan.MONTHLY),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color.Black.copy(alpha = 0.35f)
                )
            ) {
                Text(buttonTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            // 약관
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row {
                    Text("구매 시 ", fontSize = 12.sp, color = Color.Gray)
                    Text("이용약관", fontSize = 12.sp, color = Color.Gray,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://certain-exoplanet-9bc.notion.site/Cobling-Terms-of-Service-31720a2218b1807e9cf0e802f279e0bd")))
                        })
                    Text("에 동의하게 됩니다.", fontSize = 12.sp, color = Color.Gray)
                }
                Text("• 월간 구독은 자동 갱신됩니다", fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.9f))
                Text("• 갱신 24시간 전에 취소하지 않으면 자동으로 갱신됩니다", fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.9f))
                Text("• 구독은 언제든지 Google Play에서 취소할 수 있습니다", fontSize = 10.sp, color = Color.Gray.copy(alpha = 0.9f))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BenefitCard(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF3F3F3))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(36.dp), tint = Color.Black)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun PlanCard(
    title: String, subtitle: String,
    priceText: String, unitText: String,
    highlightText: String?,
    isSelected: Boolean,
    showCurrentBadge: Boolean,
    badgeColor: Color,
    onClick: () -> Unit,
    dimmed: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (dimmed) Color(0xFFF3F3F3).copy(alpha = 0.5f) else Color(0xFFF3F3F3))
            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) Color.Black else Color.Transparent, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                if (showCurrentBadge) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(badgeColor)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) { Text("현재 플랜", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(priceText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(unitText, fontSize = 14.sp, color = Color.Gray)
            }
        }
        Text(subtitle, fontSize = 14.sp, color = Color.Gray)
        HorizontalDivider(color = Color.Black.copy(alpha = 0.25f))
        highlightText?.let { ht ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("★", fontSize = 14.sp, color = Color(0xFFFFCC00))
                Text(ht, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.75f))
            }
        }
    }
}

private suspend fun setPremiumInFirestore(uid: String, plan: PremiumPlan) {
    if (uid.isEmpty()) return
    try {
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update(mapOf(
                "premium.isActive" to true,
                "premium.plan"     to plan.firestoreValue,
                "premium.source"   to "in_app",
                "premium.updatedAt" to FieldValue.serverTimestamp()
            )).await()
    } catch (e: Exception) {
        android.util.Log.e("PremiumScreen", "Update failed: ${e.message}")
    }
}
