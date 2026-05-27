package com.cobling.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobling.app.model.JournalBadgeItem
import com.cobling.app.model.JournalMemoryFragment
import com.cobling.app.ui.journal.JournalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JournalUiState(
    val isLoading: Boolean = true,
    val memoryFragments: List<JournalMemoryFragment> = emptyList(),
    val badges: List<JournalBadgeItem> = emptyList(),
    val errorMessage: String? = null
)

class JournalViewModel(
    private val repository: JournalRepository = JournalRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init {
        observeJournal()
    }

    private fun observeJournal() {
        viewModelScope.launch {
            repository.observeJournal()
                .collect { journalData ->
                    _uiState.value = JournalUiState(
                        isLoading = false,
                        memoryFragments = journalData.memoryFragments,
                        badges = journalData.badges,
                        errorMessage = null
                    )
                }
        }
    }
}