package com.cobling.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.cobling.app.ui.theme.LeeSeoyunFamily
import com.cobling.app.ui.theme.colorFromHex
import com.cobling.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// ──────────────────────────────────────
// LoginScreen
// SwiftUI LoginView → Compose
// ──────────────────────────────────────
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    initialEmail: String = "",
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    onTapSignup: () -> Unit,
    onTapResetPassword: () -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val isFormValid = email.isNotEmpty() && password.isNotEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFromHex("#FFF2DC"))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 로고 이미지
            Image(
                painter = painterResource(id = R.drawable.cobling_stage_super),
                contentDescription = "코블링 로고",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "코블링",
                fontFamily = LeeSeoyunFamily,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colorFromHex("#2B3A1E")
            )

            Spacer(modifier = Modifier.height(42.dp))

            // 이메일 입력
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        text = "이메일주소",
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = colorFromHex("#2B3A1E"),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = colorFromHex("#2B3A1E")
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 비밀번호 입력
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = {
                    Text(
                        text = "비밀번호",
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedBorderColor = colorFromHex("#2B3A1E"),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = colorFromHex("#2B3A1E")
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isFormValid) {
                            scope.launch {
                                tryLogin(
                                    authVM = authViewModel,
                                    email = email,
                                    password = password,
                                    setLoading = { isLoading = it },
                                    setError = { errorText = it },
                                    onSuccess = onLoginSuccess
                                )
                            }
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            // 비밀번호 잊음 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onTapResetPassword
                ) {
                    Text(
                        text = "비밀번호를 잊으셨나요?",
                        color = colorFromHex("#2B3A1E"),
                        fontSize = 14.sp
                    )
                }
            }

            errorText?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 로그인 버튼
            Button(
                onClick = {
                    scope.launch {
                        tryLogin(
                            authVM = authViewModel,
                            email = email,
                            password = password,
                            setLoading = { isLoading = it },
                            setError = { errorText = it },
                            onSuccess = onLoginSuccess
                        )
                    }
                },
                enabled = isFormValid && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorFromHex("#2F2F2F"),
                    disabledContainerColor = colorFromHex("#2F2F2F").copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (isLoading) "로그인 중..." else "로그인",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Divider()

            Spacer(modifier = Modifier.height(12.dp))

            // 가입하기 버튼
            TextButton(onClick = onTapSignup) {
                Text(
                    text = "계정이 없으신가요?  ",
                    color = colorFromHex("#25331B"),
                    fontSize = 16.sp
                )
                Text(
                    text = "가입하기",
                    color = colorFromHex("#25331B"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private suspend fun tryLogin(
    authVM: AuthViewModel,
    email: String,
    password: String,
    setLoading: (Boolean) -> Unit,
    setError: (String?) -> Unit,
    onSuccess: () -> Unit
) {
    setError(null)
    setLoading(true)

    try {
        authVM.signIn(email, password)
        onSuccess()
    } catch (e: Exception) {
        setError(authVM.authError.value ?: e.message)
    }

    setLoading(false)
}
