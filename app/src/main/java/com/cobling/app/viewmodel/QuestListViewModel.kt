package com.cobling.app.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobling.app.model.QuestDocument
import com.cobling.app.model.QuestStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class QuestListViewModel @Inject constructor() : ViewModel() {

    var quests by mutableStateOf<List<Triple<QuestDocument, QuestStatus, Boolean>>>(emptyList())
    var isLoading by mutableStateOf(true)
    var errorMessage by mutableStateOf<String?>(null)

    private val db = FirebaseFirestore.getInstance()

    fun fetchQuests() {
        viewModelScope.launch {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: run {
                errorMessage = "로그인 필요"
                isLoading = false
                return@launch
            }

            try {
                isLoading = true
                errorMessage = null

                val userSnap = db.collection("users")
                    .document(userId)
                    .get()
                    .await()

                val userData = userSnap.data

                val hiddenChapters = userData?.get("hiddenChapters") as? Map<*, *>
                val hidden1Data = hiddenChapters?.get("hidden1") as? Map<*, *>

                val shouldShowHidden1InQuestList =
                    hidden1Data?.get("showInQuestList") as? Boolean ?: false

                val questSnap = db.collection("quests")
                    .orderBy("order")
                    .get()
                    .await()

                val results = mutableListOf<Triple<QuestDocument, QuestStatus, Boolean>>()

                for (doc in questSnap.documents) {
                    val data = doc.data ?: continue

                    val isHidden = data["isHidden"] as? Boolean ?: false

                    if (isHidden) {
                        val canShowThisHiddenQuest =
                            doc.id == "hidden1" && shouldShowHidden1InQuestList

                        if (!canShowThisHiddenQuest) {
                            continue
                        }
                    }

                    val quest = QuestDocument(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        subtitle = data["subtitle"] as? String ?: "",
                        order = (data["order"] as? Long)?.toInt() ?: 0,
                        recommendedLevel = (data["recommendedLevel"] as? Long)?.toInt() ?: 1,
                        isActive = data["isActive"] as? Boolean ?: false,
                        allowedBlocks = (data["allowedBlocks"] as? List<*>)?.mapNotNull { it as? String }
                    )

                    val subSnap = db.collection("users")
                        .document(userId)
                        .collection("progress")
                        .document(doc.id)
                        .collection("subQuests")
                        .get()
                        .await()

                    val states = subSnap.documents.map {
                        ((it.data?.get("state") as? String) ?: "locked").trim()
                    }

                    val perfectFlags = subSnap.documents.map {
                        it.data?.get("perfectClear") as? Boolean ?: false
                    }

                    val status = when {
                        states.isNotEmpty() && states.all { it == "completed" } -> {
                            QuestStatus.COMPLETED
                        }

                        states.contains("inProgress") -> {
                            QuestStatus.IN_PROGRESS
                        }

                        else -> {
                            QuestStatus.LOCKED
                        }
                    }

                    val isPerfectChapter =
                        status == QuestStatus.COMPLETED &&
                                perfectFlags.isNotEmpty() &&
                                perfectFlags.all { it }

                    results.add(
                        Triple(
                            quest,
                            status,
                            isPerfectChapter
                        )
                    )
                }

                quests = results
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }
}