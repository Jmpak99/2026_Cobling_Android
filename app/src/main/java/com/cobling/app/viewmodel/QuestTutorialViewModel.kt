package com.cobling.app.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cobling.app.model.QuestTutorialFocusTarget
import com.cobling.app.model.QuestTutorialStep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class QuestTutorialViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cobling_tutorial", Context.MODE_PRIVATE)

    var isActive: Boolean by mutableStateOf(false)
    var currentStep: QuestTutorialStep by mutableStateOf(QuestTutorialStep.STORY_INTRO)
    var isFinished: Boolean by mutableStateOf(false)
    var isSkipped: Boolean by mutableStateOf(false)

    private var tutorialKey: String? = null

    val title: String get() = currentStep.title
    val message: String get() = currentStep.message
    val focusTarget: QuestTutorialFocusTarget? get() = currentStep.focusTarget
    val primaryButtonTitle: String get() = currentStep.primaryButtonTitle
    val showsSkipButton: Boolean get() = currentStep.showsSkipButton
    val visibleStepNumber: Int? get() = currentStep.visibleStepNumber
    val totalVisibleSteps: Int get() = currentStep.totalVisibleSteps
    val progressValue: Float get() {
        val step = visibleStepNumber ?: return 1f
        return if (totalVisibleSteps > 0) step.toFloat() / totalVisibleSteps else 0f
    }

    fun startTutorial(key: String, forceStart: Boolean = false) {
        tutorialKey = key
        if (hasSeenTutorial(key) && !forceStart) { isActive = false; isFinished = true; return }
        isActive = true; isFinished = false; isSkipped = false
        currentStep = QuestTutorialStep.STORY_INTRO
    }

    fun handlePrimaryButtonTap() {
        if (!isActive) return
        if (currentStep == QuestTutorialStep.COMPLETED) closeTutorial() else moveToNextStep()
    }

    fun goToPreviousStep() {
        if (!isActive) return
        currentStep.previousStep?.let { currentStep = it }
    }

    fun skipTutorial() {
        if (!isActive) return
        isSkipped = true; saveTutorialCompletionIfNeeded(); closeTutorial()
    }

    fun resetTutorial() {
        isActive = false; isFinished = false; isSkipped = false
        currentStep = QuestTutorialStep.STORY_INTRO; tutorialKey = null
    }

    private fun moveToNextStep() {
        val next = currentStep.nextStep ?: run { saveTutorialCompletionIfNeeded(); closeTutorial(); return }
        currentStep = next
        if (next == QuestTutorialStep.COMPLETED) { saveTutorialCompletionIfNeeded(); closeTutorial() }
    }

    private fun closeTutorial() { isActive = false; isFinished = true }
    private fun hasSeenTutorial(key: String): Boolean = prefs.getBoolean(key, false)
    private fun saveTutorialCompletionIfNeeded() { tutorialKey?.let { prefs.edit().putBoolean(it, true).apply() } }
}
