package com.cobling.app.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobling.app.util.ReviewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewManagerViewModel @Inject constructor(
    private val reviewManager: ReviewManager
) : ViewModel() {

    var shouldShowReviewPopup by mutableStateOf(false)
        private set
    var currentMilestone by mutableStateOf<Int?>(null)
        private set

    init {
        viewModelScope.launch {
            reviewManager.shouldShowReviewPopup.collectLatest { shouldShowReviewPopup = it }
        }
        viewModelScope.launch {
            reviewManager.currentMilestone.collectLatest { currentMilestone = it }
        }
    }

    fun consumePendingReviewIfNeeded() = reviewManager.consumePendingReviewIfNeeded()

    fun handleNegativeFeedback() = reviewManager.handleNegativeFeedback()

    /** activity 가 있으면 인앱 리뷰 요청, 없으면 팝업만 닫기 */
    fun handlePositiveFeedback(activity: Activity? = null) {
        if (activity != null) reviewManager.handlePositiveFeedback(activity)
        else reviewManager.dismissPopup()
    }
}
