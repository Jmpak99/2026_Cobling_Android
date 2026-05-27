package com.cobling.app.ui.journal

import com.cobling.app.model.BadgeRule
import com.cobling.app.model.JournalBadgeItem
import com.cobling.app.model.JournalMemoryFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class JournalData(
    val memoryFragments: List<JournalMemoryFragment> = emptyList(),
    val badges: List<JournalBadgeItem> = emptyList()
)

class JournalRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeJournal(): Flow<JournalData> = callbackFlow {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            trySend(
                JournalData(
                    memoryFragments = defaultMemoryFragments(),
                    badges = emptyList()
                )
            )
            close()
            return@callbackFlow
        }

        val userRef = db.collection("users").document(uid)
        val badgeRulesRef = db.collection("badgeRules")
            .whereEqualTo("isActive", true)

        var latestUserBadges: Map<*, *> = emptyMap<Any, Any>()
        var latestMemoryFragments: Map<*, *> = emptyMap<Any, Any>()
        var latestBadgeRules: List<BadgeRule> = emptyList()

        fun emitCurrentData() {
            val memoryFragments = buildMemoryFragments(latestMemoryFragments)
            val earnedBadgeIds = latestUserBadges.mapNotNull { entry ->
                val badgeId = entry.key as? String
                val badgeData = entry.value as? Map<*, *>
                val earned = badgeData?.get("earned") == true

                if (badgeId != null && earned) badgeId else null
            }.toSet()

            val badges = latestBadgeRules
                .sortedBy { it.order }
                .map { rule ->
                    JournalBadgeItem(
                        id = rule.id,
                        title = rule.title,
                        description = rule.description,
                        imageName = rule.imageName,
                        isUnlocked = earnedBadgeIds.contains(rule.id)
                    )
                }

            trySend(
                JournalData(
                    memoryFragments = memoryFragments,
                    badges = badges
                )
            )
        }

        val userListener = userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val userDoc = snapshot ?: return@addSnapshotListener

            val inventory = userDoc.get("inventory") as? Map<*, *>
            latestMemoryFragments = inventory?.get("memoryFragments") as? Map<*, *>
                ?: emptyMap<Any, Any>()

            latestUserBadges = userDoc.get("badges") as? Map<*, *>
                ?: emptyMap<Any, Any>()

            emitCurrentData()
        }

        val badgeRulesListener = badgeRulesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val docs = snapshot?.documents ?: emptyList()

            latestBadgeRules = docs.map { doc ->
                BadgeRule(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    imageName = doc.getString("imageName") ?: "",
                    type = doc.getString("type") ?: "",
                    order = doc.getLong("order") ?: 0,
                    isActive = doc.getBoolean("isActive") ?: true
                )
            }

            emitCurrentData()
        }

        awaitClose {
            userListener.remove()
            badgeRulesListener.remove()
        }
    }

    private fun buildMemoryFragments(
        memoryFragments: Map<*, *>
    ): List<JournalMemoryFragment> {
        return listOf(
            JournalMemoryFragment(
                id = "memory_fragment_1",
                title = "기억 1",
                isCollected = memoryFragments["memory_fragment_1"] == true
            ),
            JournalMemoryFragment(
                id = "memory_fragment_2",
                title = "기억 2",
                isCollected = memoryFragments["memory_fragment_2"] == true
            ),
            JournalMemoryFragment(
                id = "memory_fragment_3",
                title = "기억 3",
                isCollected = memoryFragments["memory_fragment_3"] == true
            ),
            JournalMemoryFragment(
                id = "memory_fragment_4",
                title = "기억 4",
                isCollected = memoryFragments["memory_fragment_4"] == true
            )
        )
    }

    private fun defaultMemoryFragments(): List<JournalMemoryFragment> {
        return listOf(
            JournalMemoryFragment("memory_fragment_1", "기억 1", false),
            JournalMemoryFragment("memory_fragment_2", "기억 2", false),
            JournalMemoryFragment("memory_fragment_3", "기억 3", false),
            JournalMemoryFragment("memory_fragment_4", "기억 4", false)
        )
    }
}