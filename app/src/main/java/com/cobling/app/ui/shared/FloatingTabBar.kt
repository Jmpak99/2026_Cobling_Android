package com.cobling.app.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cobling.app.R
import com.cobling.app.navigation.Screen
import com.cobling.app.viewmodel.AuthViewModel
import com.cobling.app.viewmodel.TabItem

@Composable
fun FloatingTabBar(
    selectedTab: TabItem,
    onTabSelected: (TabItem) -> Unit,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    var showLoginDialog by remember { mutableStateOf(false) }
    val isLoggedIn by authViewModel.isSignedIn.collectAsStateWithLifecycle()

    val visibleTabs = listOf(
        TabItem.QUEST,
        TabItem.JOURNAL,
        TabItem.HOME,
        TabItem.RANKING,
        TabItem.PROFILE
    )

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        visibleTabs.forEach { tab ->
            val isSelected = selectedTab == tab

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color(0xFFFFF7E9)
                        else Color.Transparent
                    )
                    .clickable {
                        handleTabSelection(
                            tab = tab,
                            isLoggedIn = isLoggedIn,
                            onSelect = onTabSelected,
                            onShowLogin = { showLoginDialog = true }
                        )
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = tabIconResId(tab)),
                        contentDescription = tab.title,
                        modifier = Modifier.size(28.dp),
                        tint = Color.Unspecified
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }
        }
    }

    if (showLoginDialog) {
        AlertDialog(
            onDismissRequest = {
                showLoginDialog = false
            },
            title = {
                Text("로그인 후 이용 가능합니다")
            },
            text = {
                Text("로그인 하시겠습니까?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLoginDialog = false
                        navController.navigate(Screen.Signup.route)
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLoginDialog = false
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

private fun handleTabSelection(
    tab: TabItem,
    isLoggedIn: Boolean,
    onSelect: (TabItem) -> Unit,
    onShowLogin: () -> Unit
) {
    if (
        !isLoggedIn &&
        (
                tab == TabItem.QUEST ||
                        tab == TabItem.RANKING ||
                        tab == TabItem.PROFILE
                )
    ) {
        onShowLogin()
        return
    }

    onSelect(tab)
}

private fun tabIconResId(tab: TabItem): Int {
    return when (tab) {
        TabItem.QUEST -> R.drawable.tab_icon_quest
        TabItem.HOME -> R.drawable.tab_icon_home
        TabItem.RANKING -> R.drawable.tab_icon_ranking
        TabItem.PROFILE -> R.drawable.tab_icon_profile
        TabItem.JOURNAL -> R.drawable.tab_icon_journal
    }
}