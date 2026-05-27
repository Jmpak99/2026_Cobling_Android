package com.cobling.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cobling.app.model.RankUser
import com.cobling.app.model.SortType
import com.cobling.app.ui.Ranking.RankingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RankingUiState(
    val users: List<RankUser> = emptyList(),
    val sortType: SortType = SortType.LEVEL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class RankingViewModel(
    private val repository: RankingRepository = RankingRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        loadRanking(SortType.LEVEL)
    }

    fun changeSortType(sortType: SortType) {
        if (_uiState.value.sortType == sortType) return

        _uiState.value = _uiState.value.copy(sortType = sortType)
        loadRanking(sortType)
    }

    private fun loadRanking(sortType: SortType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val users = repository.fetchRanking(sortType)

                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "랭킹을 불러오지 못했습니다."
                )
            }
        }
    }
}