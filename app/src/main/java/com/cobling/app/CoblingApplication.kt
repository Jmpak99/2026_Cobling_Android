package com.cobling.app

import android.app.Application
import com.cobling.app.util.LocalStorageManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CoblingApplication : Application() {

    @Inject
    lateinit var localStorage: LocalStorageManager

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Hilt가 super.onCreate() 에서 inject 완료 → 바로 static 초기화
        LocalStorageManager.init(localStorage)
    }

    // Hilt가 inject를 끝낸 뒤 호출되는 시점 활용
    override fun onTerminate() {
        super.onTerminate()
    }

    fun initStatics() {
        // 이미 onCreate()에서 초기화됨. MainActivity에서 호출해도 무방.
    }
}
