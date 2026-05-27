package com.cobling.app.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cobling.app.R
import com.cobling.app.ui.theme.colorFromHex

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                imageRes = R.drawable.cobling_stage_legend,
                title = "코블링이 당신을 기다려요",
                description = "깊은 잠에 빠진 작은 생명체\n당신의 손끝에서 깨어날 준비를 하고 있어요"
            ),
            OnboardingPage(
                imageRes = R.drawable.cobling_stage_legend,
                title = "블록을 조립해 길을 만들어요",
                description = "앞으로 가기, 회전하기, 반복하기\n블록을 연결하며 코딩의 흐름을 배워요"
            ),
            OnboardingPage(
                imageRes = R.drawable.cobling_stage_legend,
                title = "퀘스트를 클리어하고 성장해요",
                description = "문제를 해결할수록 코블링이 성장해요\n지금 바로 첫 모험을 시작해보세요"
            )
        )
    }

    var currentPage by remember { mutableStateOf(0) }

    val page = pages[currentPage]
    val isLastPage = currentPage == pages.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(212.dp))

            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = "온보딩 캐릭터",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = page.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                color = colorFromHex("#222222"),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = page.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp),
                color = colorFromHex("#666666"),
                fontSize = 14.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.forEachIndexed { index, _ ->
                    Text(
                        text = "●",
                        color = if (index == currentPage) {
                            colorFromHex("#333333")
                        } else {
                            colorFromHex("#BDBDBD")
                        },
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        currentPage += 1
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorFromHex("#233300")
                )
            ) {
                Text(
                    text = if (isLastPage) "시작하기 >" else "다음으로 >",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}