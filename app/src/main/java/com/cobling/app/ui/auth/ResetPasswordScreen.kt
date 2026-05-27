package com.cobling.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.ui.theme.colorFromHex
import com.cobling.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ResetPasswordScreen(
    authViewModel: AuthViewModel,
    initialEmail: String = "",
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var isLoading by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFromHex("#F7F4EE"))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "비밀번호 재설정",
                color = colorFromHex("#222222"),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "가입하신 이메일 주소를 입력하시면\n비밀번호 재설정 메일을 보내드립니다.",
                color = colorFromHex("#8C877F"),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    messageText = null
                    isSuccess = false
                },
                placeholder = {
                    Text(
                        text = "이메일을 입력하세요",
                        color = colorFromHex("#B1AAA0")
                    )
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = colorFromHex("#E6BE63"),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = colorFromHex("#222222"),
                    unfocusedTextColor = colorFromHex("#222222"),
                    cursorColor = colorFromHex("#E6BE63")
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()

                        scope.launch {
                            sendResetPasswordEmail(
                                authViewModel = authViewModel,
                                email = email,
                                setLoading = { isLoading = it },
                                setMessage = { text, success ->
                                    messageText = text
                                    isSuccess = success
                                }
                            )
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            messageText?.let {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = it,
                    color = if (isSuccess) colorFromHex("#2B3A1E") else Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        sendResetPasswordEmail(
                            authViewModel = authViewModel,
                            email = email,
                            setLoading = { isLoading = it },
                            setMessage = { text, success ->
                                messageText = text
                                isSuccess = success
                            }
                        )
                    }
                },
                enabled = email.isNotBlank() && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorFromHex("#E6BE63"),
                    disabledContainerColor = colorFromHex("#E6BE63").copy(alpha = 0.45f)
                )
            ) {
                Text(
                    text = if (isLoading) "전송 중..." else "재설정 메일 보내기",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onBack
            ) {
                Text(
                    text = "로그인 화면으로 돌아가기",
                    color = colorFromHex("#8C877F"),
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

private suspend fun sendResetPasswordEmail(
    authViewModel: AuthViewModel,
    email: String,
    setLoading: (Boolean) -> Unit,
    setMessage: (String, Boolean) -> Unit
) {
    val trimmedEmail = email.trim()

    if (trimmedEmail.isEmpty()) {
        setMessage("이메일을 입력해 주세요.", false)
        return
    }

    setLoading(true)

    try {
        authViewModel.resetPassword(trimmedEmail)
        setMessage("비밀번호 재설정 메일을 전송했습니다.", true)
    } catch (e: Exception) {
        setMessage(
            authViewModel.authError.value ?: e.message ?: "메일 전송에 실패했습니다.",
            false
        )
    }

    setLoading(false)
}