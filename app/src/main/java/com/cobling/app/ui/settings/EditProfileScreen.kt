package com.cobling.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.TabBarViewModel
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    authViewModel: AuthViewModel,
    tabBarViewModel: TabBarViewModel,
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val currentEmail by authViewModel.currentUserEmail.collectAsStateWithLifecycle()

    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deletePassword by remember { mutableStateOf("") }

    LaunchedEffect(userProfile, currentEmail) {
        if (nickname.isEmpty()) {
            nickname = userProfile?.nickname ?: ""
        }

        if (email.isEmpty()) {
            email = currentEmail ?: ""
        }
    }

    LaunchedEffect(Unit) {
        tabBarViewModel.isTabBarVisible = false
    }

    DisposableEffect(Unit) {
        onDispose {
            tabBarViewModel.isTabBarVisible = true
        }
    }

    val passwordRegex = Regex("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{8,}$")
    val isPasswordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword
    val isPasswordValid = password.isEmpty() || passwordRegex.matches(password)
    val isFormValid = nickname.isNotEmpty() && !isPasswordMismatch && isPasswordValid

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로"
                    )
                }

                Text(
                    text = "내 정보 수정",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LabelField("닉네임") {
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = {
                            nickname = it
                        },
                        placeholder = {
                            Text("닉네임을 입력하세요.")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                LabelField("이메일") {
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        placeholder = {
                            Text("이메일을 입력하세요")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                LabelField("비밀번호 변경 (선택)") {
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                        },
                        placeholder = {
                            Text("영어소문자, 숫자 포함 8자 이상")
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    if (password.isNotEmpty() && !isPasswordValid) {
                        Text(
                            text = "영어 소문자와 숫자를 포함해 8자 이상 입력해주세요.",
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }
                }

                LabelField("비밀번호 확인") {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                        },
                        placeholder = {
                            Text("비밀번호를 다시 입력하세요")
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    if (isPasswordMismatch) {
                        Text(
                            text = "비밀번호가 일치하지 않습니다.",
                            color = Color.Red,
                            fontSize = 13.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true

                            try {
                                authViewModel.updateNickname(nickname)

                                val curEmail = currentEmail
                                if (email.isNotEmpty() && email != curEmail) {
                                    authViewModel.updateEmail(email)
                                }

                                if (password.isNotEmpty()) {
                                    authViewModel.updatePassword(password)
                                }

                                onBack()
                            } catch (e: Exception) {
                                alertMessage = e.message ?: "오류가 발생했습니다."
                                showAlert = true
                            }

                            isSaving = false
                        }
                    },
                    enabled = isFormValid && !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormValid) {
                            Color(0xFFE9E8DD)
                        } else {
                            Color.Gray.copy(alpha = 0.2f)
                        }
                    )
                ) {
                    Text(
                        text = if (isSaving) "저장 중..." else "완료",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(40.dp))
            }

            TextButton(
                onClick = {
                    showDeleteConfirm = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp)
            ) {
                Text(
                    text = "탈퇴하기",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = {
                    if (!isDeleting) {
                        showDeleteConfirm = false
                        deletePassword = ""
                    }
                },
                title = {
                    Text("정말 탈퇴하시겠어요?")
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("계정과 데이터가 영구적으로 삭제됩니다.\n이 작업은 되돌릴 수 없습니다.")

                        OutlinedTextField(
                            value = deletePassword,
                            onValueChange = {
                                deletePassword = it
                            },
                            placeholder = {
                                Text("비밀번호를 입력하세요")
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = !isDeleting && deletePassword.isNotBlank(),
                        onClick = {
                            scope.launch {
                                isDeleting = true

                                try {
                                    authViewModel.deleteAccountWithPassword(deletePassword)

                                    showDeleteConfirm = false
                                    deletePassword = ""

                                    onAccountDeleted()
                                } catch (e: Exception) {
                                    alertMessage = when {
                                        e.message?.contains(
                                            "auth credential is incorrect",
                                            ignoreCase = true
                                        ) == true -> {
                                            "비밀번호가 올바르지 않습니다."
                                        }

                                        e.message?.contains(
                                            "password is invalid",
                                            ignoreCase = true
                                        ) == true -> {
                                            "비밀번호가 올바르지 않습니다."
                                        }

                                        e.message?.contains(
                                            "network",
                                            ignoreCase = true
                                        ) == true -> {
                                            "네트워크 연결을 확인해주세요."
                                        }

                                        else -> {
                                            e.message ?: "탈퇴 처리 중 오류가 발생했습니다."
                                        }
                                    }

                                    showAlert = true
                                }

                                isDeleting = false
                            }
                        }
                    ) {
                        Text(
                            text = if (isDeleting) "처리 중..." else "탈퇴",
                            color = Color.Red
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isDeleting,
                        onClick = {
                            showDeleteConfirm = false
                            deletePassword = ""
                        }
                    ) {
                        Text("취소")
                    }
                }
            )
        }

        if (showAlert) {
            AlertDialog(
                onDismissRequest = {
                    showAlert = false
                },
                title = {
                    Text("알림")
                },
                text = {
                    Text(alertMessage)
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAlert = false
                        }
                    ) {
                        Text("확인")
                    }
                }
            )
        }
    }
}

@Composable
private fun LabelField(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        content()
    }
}