package com.cobling.app.model

data class RankUser(
    val id: String = "",
    val name: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    val quests: Int = 0,
    val tier: String = "C",
    val profileImageURL: String = ""
)

enum class SortType(val label: String) {
    LEVEL("레벨순"),
    XP("경험치순"),
    QUESTS("퀘스트순")
}