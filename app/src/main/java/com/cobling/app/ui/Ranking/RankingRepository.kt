package com.cobling.app.ui.Ranking

import com.cobling.app.model.RankUser
import com.cobling.app.model.SortType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class RankingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun fetchRanking(sortType: SortType): List<RankUser> {
        val query = when (sortType) {
            SortType.LEVEL -> db.collection("rankings")
                .orderBy("level", Query.Direction.DESCENDING)
                .orderBy("xp", Query.Direction.DESCENDING)

            SortType.XP -> db.collection("rankings")
                .orderBy("xp", Query.Direction.DESCENDING)

            SortType.QUESTS -> db.collection("rankings")
                .orderBy("quests", Query.Direction.DESCENDING)
                .orderBy("level", Query.Direction.DESCENDING)
        }

        val snapshot = query
            .limit(50)
            .get()
            .await()

        return snapshot.documents.map { document ->
            RankUser(
                id = document.id,
                name = document.getString("name") ?: "이름 없음",
                level = document.getLong("level")?.toInt() ?: 1,
                tier = document.getString("tier") ?: "C",
                xp = document.getLong("xp")?.toInt() ?: 0,
                quests = document.getLong("quests")?.toInt() ?: 0,
                profileImageURL = document.getString("profileImageURL") ?: ""
            )
        }
    }
}