package com.jogpal.app.features.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jogpal.app.domain.run.RunPlan
import com.jogpal.app.domain.run.RunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class RunHistoryUiState(
    val isLoading: Boolean = false,
    val runs: List<RunPlan> = emptyList(),
    val error: String? = null
)

class RunHistoryViewModel(
    private val runRepository: RunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunHistoryUiState())
    val uiState: StateFlow<RunHistoryUiState> = _uiState.asStateFlow()

    init {
        loadRunHistory()
    }

    private fun loadRunHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runRepository.getRunHistory().collectLatest { history ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    runs = history
                )
            }
        }
    }
}
