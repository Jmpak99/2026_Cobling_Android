package com.cobling.app.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _exp = MutableStateFlow(0.0)
    val exp: StateFlow<Double> = _exp.asStateFlow()

    private val _expPercent = MutableStateFlow(0.0)
    val expPercent: StateFlow<Double> = _expPercent.asStateFlow()

    private val _dailyCount = MutableStateFlow(0)
    val dailyCount: StateFlow<Int> = _dailyCount.asStateFlow()

    private val _dailyIsCompleted = MutableStateFlow(false)
    val dailyIsCompleted: StateFlow<Boolean> = _dailyIsCompleted.asStateFlow()

    private val _monthlyCount = MutableStateFlow(0)
    val monthlyCount: StateFlow<Int> = _monthlyCount.asStateFlow()

    private val _monthlyIsCompleted = MutableStateFlow(false)
    val monthlyIsCompleted: StateFlow<Boolean> = _monthlyIsCompleted.asStateFlow()

    // missions DB 설정값
    private val _dailyIsEnabled = MutableStateFlow(true)
    val dailyIsEnabled: StateFlow<Boolean> = _dailyIsEnabled.asStateFlow()

    private val _dailyTitle = MutableStateFlow("오늘의 미션")
    val dailyTitle: StateFlow<String> = _dailyTitle.asStateFlow()

    private val _dailySubtitle = MutableStateFlow("두 문제 이상 풀기")
    val dailySubtitle: StateFlow<String> = _dailySubtitle.asStateFlow()

    private val _dailyTargetCount = MutableStateFlow(2)
    val dailyTargetCount: StateFlow<Int> = _dailyTargetCount.asStateFlow()

    private val _dailyRewardExp = MutableStateFlow(120)
    val dailyRewardExp: StateFlow<Int> = _dailyRewardExp.asStateFlow()

    private val _monthlyIsEnabled = MutableStateFlow(true)
    val monthlyIsEnabled: StateFlow<Boolean> = _monthlyIsEnabled.asStateFlow()

    private val _monthlyTitle = MutableStateFlow("월간 미션")
    val monthlyTitle: StateFlow<String> = _monthlyTitle.asStateFlow()

    private val _monthlySubtitle = MutableStateFlow("1챕터 이상 끝내기")
    val monthlySubtitle: StateFlow<String> = _monthlySubtitle.asStateFlow()

    private val _monthlyTargetCount = MutableStateFlow(1)
    val monthlyTargetCount: StateFlow<Int> = _monthlyTargetCount.asStateFlow()

    private val _monthlyRewardExp = MutableStateFlow(400)
    val monthlyRewardExp: StateFlow<Int> = _monthlyRewardExp.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private var isListening = false
    private var dailyListener: ListenerRegistration? = null
    private var monthlyListener: ListenerRegistration? = null
    private var dailyConfigListener: ListenerRegistration? = null
    private var monthlyConfigListener: ListenerRegistration? = null

    fun startListeningUserData() {
        if (isListening) return
        isListening = true
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            isListening = false; return
        }

        listener = db.collection("users").document(userId).addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            val data = snap.data ?: return@addSnapshotListener
            val newLevel = (data["level"] as? Long)?.toInt() ?: 1
            val newExp = data["exp"] as? Double ?: (data["exp"] as? Long)?.toDouble() ?: 0.0
            _level.value = newLevel
            _exp.value = newExp
            val req = maxExpForLevel(newLevel)
            _expPercent.value = (newExp / req).coerceIn(0.0, 1.0)
        }

        val base = db.collection("users").document(userId).collection("missionProgress")
        dailyListener = base.document("daily").addSnapshotListener { snap, err ->
            if (err != null || snap?.data == null) { _dailyCount.value = 0; _dailyIsCompleted.value = false; return@addSnapshotListener }
            val d = snap.data!!
            _dailyCount.value = (d["count"] as? Long)?.toInt() ?: 0
            _dailyIsCompleted.value = d["isCompleted"] as? Boolean ?: false
        }
        monthlyListener = base.document("monthly").addSnapshotListener { snap, err ->
            if (err != null || snap?.data == null) { _monthlyCount.value = 0; _monthlyIsCompleted.value = false; return@addSnapshotListener }
            val d = snap.data!!
            _monthlyCount.value = (d["count"] as? Long)?.toInt() ?: 0
            _monthlyIsCompleted.value = d["isCompleted"] as? Boolean ?: false
        }

        val missions = db.collection("missions")
        dailyConfigListener = missions.document("daily").addSnapshotListener { snap, err ->
            if (err != null || snap?.data == null) return@addSnapshotListener
            val d = snap.data!!
            _dailyIsEnabled.value = d["isEnabled"] as? Boolean ?: true
            _dailyTitle.value = d["title"] as? String ?: "오늘의 미션"
            _dailySubtitle.value = d["subtitle"] as? String ?: "두 문제 이상 풀기"
            _dailyTargetCount.value = (d["targetCount"] as? Long)?.toInt() ?: 2
            _dailyRewardExp.value = (d["rewardExp"] as? Long)?.toInt() ?: 120
        }
        monthlyConfigListener = missions.document("monthly").addSnapshotListener { snap, err ->
            if (err != null || snap?.data == null) return@addSnapshotListener
            val d = snap.data!!
            _monthlyIsEnabled.value = d["isEnabled"] as? Boolean ?: true
            _monthlyTitle.value = d["title"] as? String ?: "월간 미션"
            _monthlySubtitle.value = d["subtitle"] as? String ?: "1챕터 이상 끝내기"
            _monthlyTargetCount.value = (d["targetCount"] as? Long)?.toInt() ?: 1
            _monthlyRewardExp.value = (d["rewardExp"] as? Long)?.toInt() ?: 400
        }
    }

    fun stopListeningUserData() {
        listener?.remove(); listener = null
        dailyListener?.remove(); dailyListener = null
        monthlyListener?.remove(); monthlyListener = null
        dailyConfigListener?.remove(); dailyConfigListener = null
        monthlyConfigListener?.remove(); monthlyConfigListener = null
        isListening = false
    }

    private fun maxExpForLevel(level: Int): Double {
        val table = mapOf(1 to 100.0, 2 to 120.0, 3 to 160.0, 4 to 200.0, 5 to 240.0,
            6 to 310.0, 7 to 380.0, 8 to 480.0, 9 to 600.0, 10 to 750.0,
            11 to 930.0, 12 to 1160.0, 13 to 1460.0, 14 to 1820.0, 15 to 2270.0,
            16 to 2840.0, 17 to 3550.0, 18 to 4440.0, 19 to 5550.0)
        return table[level] ?: 100.0
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningUserData()
    }
}
