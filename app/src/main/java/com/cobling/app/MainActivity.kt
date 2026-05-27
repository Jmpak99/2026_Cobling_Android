package com.cobling.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cobling.app.ui.AppRootView
import com.cobling.app.ui.theme.CoblingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // static 참조 초기화 (Hilt inject 완료 후)
        (application as? CoblingApplication)?.initStatics()
        setContent {
            CoblingTheme {
                AppRootView()
            }
        }
    }
}
