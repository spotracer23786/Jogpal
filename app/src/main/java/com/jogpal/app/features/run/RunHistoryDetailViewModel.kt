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

data class RunHistoryDetailUiState(
    val isLoading: Boolean = false,
    val run: RunPlan? = null,
    val error: String? = null
)

class RunHistoryDetailViewModel(
    private val runRepository: RunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunHistoryDetailUiState())
    val uiState: StateFlow<RunHistoryDetailUiState> = _uiState.asStateFlow()

    fun loadRun(runId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            runRepository.getRunPlan(runId).collectLatest { plan ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    run = plan
                )
            }
        }
    }
}
