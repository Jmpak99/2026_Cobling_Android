package com.cobling.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cobling.app.viewmodel.ContributionFormViewModel
import com.cobling.app.viewmodel.ContributionThanksViewModel
import com.cobling.app.viewmodel.ContributionType
import java.text.SimpleDateFormat
import java.util.Locale

// ─── ContributionThanksScreen ────────────────────────────────────────────────
@Composable
fun ContributionThanksScreen(
    onBack: () -> Unit,
    vm: ContributionThanksViewModel = hiltViewModel()
) {
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.start() }
    DisposableEffect(Unit) { onDispose { vm.stop() } }

    if (showForm) {
        ContributionFormScreen(onBack = { showForm = false })
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(50.dp))
            Text("기여해 주신 분", fontSize = 36.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.height(20.dp))
            Text(
                "주신 아이디어가 서비스에 반영이 된다면\n해당 페이지에 업데이트 됩니다. :)",
                fontSize = 16.sp, color = Color.Gray,
                textAlign = TextAlign.Center, lineHeight = 22.sp
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { showForm = true },
                modifier = Modifier.size(200.dp, 56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD475))
            ) { Text("나도 기여하기", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
            Spacer(Modifier.height(36.dp))
        }
        HorizontalDivider()

        when {
            vm.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("불러오는 중…", color = Color.Gray)
                }
            }
            vm.errorMessage != null ->
                Text("불러오기에 실패했어요\n${vm.errorMessage}", Modifier.padding(24.dp))
            vm.items.isEmpty() ->
                Text("아직 등록된 기여가 없어요\n첫 번째 기여자가 되어주세요! 😊", Modifier.padding(24.dp))
            else -> LazyColumn(contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)) {
                items(vm.items) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (item.contributorsText.isNotEmpty())
                                Text(item.contributorsText, fontSize = 14.sp, color = Color.Gray)
                        }
                        Text(
                            SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(item.date),
                            fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, start = 12.dp)
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

// ─── ContributionFormScreen ──────────────────────────────────────────────────
@Composable
fun ContributionFormScreen(
    onBack: () -> Unit,
    vm: ContributionFormViewModel = hiltViewModel()
) {
    LaunchedEffect(vm.didSubmit) { if (vm.didSubmit) onBack() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
            }
            Spacer(Modifier.weight(1f))
            Text("기여하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("*닉네임 (최대 15자)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = vm.nickname, onValueChange = { vm.nickname = it; vm.enforceLimits() },
                    placeholder = { Text("닉네임을 입력해주세요") },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("*기여 유형", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                ContributionType.entries.forEach { t ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = vm.type == t, onClick = { vm.type = t })
                        Spacer(Modifier.width(8.dp))
                        Text(t.label, fontSize = 16.sp)
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("*내용 (최대 500자)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = vm.content, onValueChange = { vm.content = it; vm.enforceLimits() },
                    placeholder = { Text("내용을 입력해주세요") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(12.dp), maxLines = 10
                )
                Text("${vm.content.length}/500", fontSize = 14.sp, color = Color.Gray,
                    modifier = Modifier.align(Alignment.End))
            }
        }

        HorizontalDivider(color = Color.Black.copy(alpha = 0.15f))
        Button(
            onClick = { vm.submit() },
            enabled = vm.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = if (vm.canSubmit) 0.75f else 0.25f)
            )
        ) {
            if (vm.isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("제출하기", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    vm.alertMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { vm.alertMessage = null },
            title = { Text("알림") }, text = { Text(msg) },
            confirmButton = { TextButton(onClick = { vm.alertMessage = null }) { Text("확인") } }
        )
    }
}
