package com.cobling.app.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("cobling_review", Context.MODE_PRIVATE)

    private val subQuestClearCountKey = "review.subQuestClearCount"
    private val reviewRequestedMilestonesKey = "review.requestedMilestones"
    private val pendingReviewMilestoneKey = "review.pendingMilestone"
    private val reviewMilestones: Set<Int> = setOf(5, 15, 30)

    private val _shouldShowReviewPopup = MutableStateFlow(false)
    val shouldShowReviewPopup: StateFlow<Boolean> = _shouldShowReviewPopup.asStateFlow()

    private val _currentMilestone = MutableStateFlow<Int?>(null)
    val currentMilestone: StateFlow<Int?> = _currentMilestone.asStateFlow()

    val currentClearCount: Int
        get() = prefs.getInt(subQuestClearCountKey, 0)

    val requestedMilestones: Set<Int>
        get() {
            val str = prefs.getString(reviewRequestedMilestonesKey, "") ?: ""
            return if (str.isEmpty()) emptySet()
            else str.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        }

    val pendingMilestone: Int?
        get() {
            val value = prefs.getInt(pendingReviewMilestoneKey, 0)
            return if (value == 0) null else value
        }

    fun recordSubQuestCompletion() {
        val newCount = currentClearCount + 1
        prefs.edit().putInt(subQuestClearCountKey, newCount).apply()
        checkReviewTrigger(newCount)
    }

    fun checkReviewTrigger(count: Int) {
        if (!reviewMilestones.contains(count)) return
        if (hasRequestedReview(count)) return
        prefs.edit().putInt(pendingReviewMilestoneKey, count).apply()
        markReviewRequested(count)
    }

    fun consumePendingReviewIfNeeded() {
        val milestone = pendingMilestone ?: return
        _currentMilestone.value = milestone
        _shouldShowReviewPopup.value = true
        prefs.edit().remove(pendingReviewMilestoneKey).apply()
    }

    fun handlePositiveFeedback(activity: Activity) {
        _shouldShowReviewPopup.value = false
        requestInAppReview(activity)
    }

    fun handleNegativeFeedback() {
        _shouldShowReviewPopup.value = false
    }

    fun dismissPopup() {
        _shouldShowReviewPopup.value = false
    }

    fun hasRequestedReview(milestone: Int): Boolean = requestedMilestones.contains(milestone)

    private fun markReviewRequested(milestone: Int) {
        val updated = requestedMilestones.toMutableSet()
        updated.add(milestone)
        prefs.edit()
            .putString(reviewRequestedMilestonesKey, updated.sorted().joinToString(","))
            .apply()
    }

    private fun requestInAppReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo: ReviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
            }
        }
    }

    fun resetAllReviewData() {
        prefs.edit()
            .remove(subQuestClearCountKey)
            .remove(reviewRequestedMilestonesKey)
            .remove(pendingReviewMilestoneKey)
            .apply()
        _shouldShowReviewPopup.value = false
        _currentMilestone.value = null
    }

    fun setClearCountForDebug(count: Int) {
        prefs.edit().putInt(subQuestClearCountKey, count).apply()
    }
}
