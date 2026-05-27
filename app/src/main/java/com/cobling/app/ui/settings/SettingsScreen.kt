package com.cobling.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.cobling.app.navigation.Screen
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.TabBarViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    tabBarViewModel: TabBarViewModel,
    navController: NavHostController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val currentEmail by authViewModel.currentUserEmail.collectAsStateWithLifecycle()

    var showLogoutConfirm by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    var showCutscenePreview by remember { mutableStateOf(false) }

    LaunchedEffect(showCutscenePreview) {
        tabBarViewModel.isTabBarVisible = !showCutscenePreview
    }

    if (showCutscenePreview) {
        CutscenePreviewScreen(
            authViewModel = authViewModel,
            onBack = {
                showCutscenePreview = false
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "설정",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.25f))
                )

                Column(modifier = Modifier.weight(1f)) {
                    val nick = userProfile?.nickname?.takeIf { it.isNotEmpty() }
                        ?: "닉네임을 설정해 주세요"

                    Text(
                        text = nick,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = currentEmail ?: "",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                TextButton(
                    onClick = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color(0xFFE9E8DD)
                    )
                ) {
                    Text(
                        text = "내 정보 수정",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "서비스 정보",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
            ) {
                ServiceRow(
                    icon = Icons.Filled.Group,
                    title = "코블링에 기여해주세요!",
                    onClick = {
                        navController.navigate(Screen.ContributionThanks.route)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(start = 52.dp))

                ServiceRow(
                    icon = Icons.Filled.PlayCircle,
                    title = "튜토리얼 다시보기",
                    onClick = {
                        showCutscenePreview = true
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(start = 52.dp))

                ServiceRow(
                    icon = Icons.Filled.Lock,
                    title = "개인정보 처리방침",
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://certain-exoplanet-9bc.notion.site/Cobling-Privacy-Policy-31720a2218b1808783b3da4379d1ec9f"
                                )
                            )
                        )
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(start = 52.dp))

                ServiceRow(
                    icon = Icons.Filled.Lock,
                    title = "이용약관",
                    onClick = {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    "https://certain-exoplanet-9bc.notion.site/Cobling-Privacy-Policy-31720a2218b1808783b3da4379d1ec9f"
                                )
                            )
                        )
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(start = 52.dp))

                ServiceRow(
                    icon = Icons.Filled.Info,
                    title = "버전 정보",
                    trailingText = "1.0.0v",
                    showChevron = false,
                    onClick = {}
                )

                HorizontalDivider(modifier = Modifier.padding(start = 52.dp))

                ServiceRow(
                    icon = Icons.Filled.Lock,
                    title = "로그아웃",
                    titleColor = Color.Red,
                    showChevron = false,
                    onClick = {
                        showLogoutConfirm = true
                    }
                )
            }

            Spacer(Modifier.height(100.dp))
        }

        if (showLogoutConfirm) {
            AlertDialog(
                onDismissRequest = {
                    showLogoutConfirm = false
                },
                title = {
                    Text("로그아웃")
                },
                text = {
                    Text("정말 로그아웃 하시려구요?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isLoggingOut = true
                                showLogoutConfirm = false
                                authViewModel.signOut()
                                isLoggingOut = false
                            }
                        }
                    ) {
                        Text(
                            text = if (isLoggingOut) "로그아웃 중..." else "로그아웃",
                            color = Color.Red
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showLogoutConfirm = false
                        }
                    ) {
                        Text("취소")
                    }
                }
            )
        }
    }
}

@Composable
private fun ServiceRow(
    icon: ImageVector,
    title: String,
    trailingText: String? = null,
    showChevron: Boolean = true,
    titleColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(28.dp),
            tint = titleColor
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )

        trailingText?.let {
            Text(
                text = it,
                fontSize = 16.sp,
                color = Color.Gray
            )
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray.copy(alpha = 0.8f)
            )
        }
    }
}