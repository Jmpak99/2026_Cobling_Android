package com.cobling.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

// ─── ContributionThanks ──────────────────────────────────────────────────────
data class ContributionThanksItem(
    val id: String,
    val title: String,
    val contributorsText: String,
    val date: Date
)

@HiltViewModel
class ContributionThanksViewModel @Inject constructor() : ViewModel() {
    var items by mutableStateOf<List<ContributionThanksItem>>(emptyList())
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)
    private var listener: ListenerRegistration? = null

    fun start() {
        isLoading = true
        listener = FirebaseFirestore.getInstance()
            .collection("contributionThanks")
            .whereEqualTo("isPublished", true)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { errorMessage = err.message; isLoading = false; return@addSnapshotListener }
                items = snap?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val title = (data["title"] as? String)?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                    val contributorsText = (data["contributorsText"] as? String)?.trim() ?: ""
                    val ts = data["date"] as? com.google.firebase.Timestamp
                    ContributionThanksItem(doc.id, title, contributorsText, ts?.toDate() ?: Date())
                } ?: emptyList()
                isLoading = false
            }
    }

    fun stop() { listener?.remove(); listener = null }
    override fun onCleared() { stop(); super.onCleared() }
}

// ─── ContributionForm ────────────────────────────────────────────────────────
enum class ContributionType(val label: String) { BUG("버그 제보"), IDEA("아이디어 제안") }

@HiltViewModel
class ContributionFormViewModel @Inject constructor() : ViewModel() {
    var nickname    by mutableStateOf("")
    var type        by mutableStateOf(ContributionType.IDEA)
    var content     by mutableStateOf("")
    var isSubmitting by mutableStateOf(false)
    var alertMessage by mutableStateOf<String?>(null)
    var didSubmit   by mutableStateOf(false)

    val canSubmit get() = !isSubmitting &&
            nickname.trim().isNotEmpty() && content.trim().isNotEmpty() &&
            nickname.length <= 15 && content.length <= 500

    fun enforceLimits() {
        if (nickname.length > 15) nickname = nickname.take(15)
        if (content.length > 500) content = content.take(500)
    }

    fun submit() {
        viewModelScope.launch {
            if (!canSubmit) return@launch
            isSubmitting = true
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
            try {
                FirebaseFirestore.getInstance().collection("contributionSubmissions").add(
                    mapOf(
                        "nickname"   to nickname.trim(),
                        "type"       to type.label,
                        "content"    to content.trim(),
                        "uid"        to uid,
                        "createdAt"  to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
                didSubmit = true
            } catch (e: Exception) {
                alertMessage = e.message
            }
            isSubmitting = false
        }
    }
}
