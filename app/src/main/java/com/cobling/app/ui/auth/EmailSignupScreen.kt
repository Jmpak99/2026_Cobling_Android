package com.cobling.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.ui.theme.colorFromHex
import com.cobling.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

// ──────────────────────────────────────
// EmailSignupScreen
// XML 회원가입 화면 디자인 → Compose
// ──────────────────────────────────────
@Composable
fun EmailSignupScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onSignupSuccess: () -> Unit,
    onTapLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val isPasswordMismatch = confirm.isNotEmpty() && password != confirm

    val isPasswordValid =
        password.isEmpty() || Regex("^(?=.*[a-z])(?=.*\\d)[a-z\\d]{8,}$")
            .matches(password)

    val isFormValid =
        name.isNotBlank() &&
                email.contains("@") &&
                password.length >= 8 &&
                isPasswordValid &&
                password == confirm

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFromHex("#F7F4EE"))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 14.dp, end = 14.dp, top = 28.dp, bottom = 32.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = colorFromHex("#EEE7DB"),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "프로필",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorFromHex("#222222")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "가입하는 분의 프로필을 입력해주세요.",
                    fontSize = 13.sp,
                    color = colorFromHex("#8C877F")
                )

                Spacer(modifier = Modifier.height(24.dp))

                SignupLabel(text = "닉네임")

                Spacer(modifier = Modifier.height(8.dp))

                SignupTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "닉네임을 입력하세요.",
                    keyboardType = KeyboardType.Text
                )

                Spacer(modifier = Modifier.height(16.dp))

                SignupLabel(text = "이메일")

                Spacer(modifier = Modifier.height(8.dp))

                SignupTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "email@email.com",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                SignupLabel(text = "비밀번호")

                Spacer(modifier = Modifier.height(8.dp))

                SignupPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "영어소문자, 숫자 포함 8자 이상"
                )

                if (password.isNotEmpty() && !isPasswordValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "영어 소문자와 숫자를 포함해 8자 이상 입력해주세요.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SignupLabel(text = "비밀번호 확인")

                Spacer(modifier = Modifier.height(8.dp))

                SignupPasswordField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    placeholder = "비밀번호를 다시 입력하세요"
                )

                if (isPasswordMismatch) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "비밀번호가 일치하지 않습니다.",
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                errorText?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "가입을 완료하면 코블링의 이용약관 및 개인정보처리방침에 동의하는 것으로 간주됩니다.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = colorFromHex("#8C877F")
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            errorText = null

                            try {
                                authViewModel.signUp(email, password, name)
                                onSignupSuccess()
                            } catch (e: Exception) {
                                errorText = authViewModel.authError.value ?: e.message
                            }

                            isLoading = false
                        }
                    },
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorFromHex("#E6BE63"),
                        disabledContainerColor = colorFromHex("#E6BE63").copy(alpha = 0.45f)
                    )
                ) {
                    Text(
                        text = if (isLoading) "가입 중..." else "가입하기",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "이미 계정이 있으신가요? 로그인",
                    fontSize = 13.sp,
                    color = colorFromHex("#6F695F"),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable {
                            onTapLogin()
                        }
                )
            }
        }
    }
}

@Composable
private fun SignupLabel(
    text: String
) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = colorFromHex("#222222")
    )
}

@Composable
private fun SignupTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = colorFromHex("#A6A19A"),
                fontSize = 15.sp
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorFromHex("#FBFAF8"),
            unfocusedContainerColor = colorFromHex("#FBFAF8"),
            disabledContainerColor = colorFromHex("#FBFAF8"),
            focusedBorderColor = colorFromHex("#DDD2C4"),
            unfocusedBorderColor = colorFromHex("#DDD2C4"),
            cursorColor = colorFromHex("#222222"),
            focusedTextColor = colorFromHex("#222222"),
            unfocusedTextColor = colorFromHex("#222222")
        )
    )
}

@Composable
private fun SignupPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = colorFromHex("#A6A19A"),
                fontSize = 15.sp
            )
        },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colorFromHex("#FBFAF8"),
            unfocusedContainerColor = colorFromHex("#FBFAF8"),
            disabledContainerColor = colorFromHex("#FBFAF8"),
            focusedBorderColor = colorFromHex("#DDD2C4"),
            unfocusedBorderColor = colorFromHex("#DDD2C4"),
            cursorColor = colorFromHex("#222222"),
            focusedTextColor = colorFromHex("#222222"),
            unfocusedTextColor = colorFromHex("#222222")
        )
    )
}