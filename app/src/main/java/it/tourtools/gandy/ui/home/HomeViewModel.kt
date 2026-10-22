package it.tourtools.gandy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.tourtools.gandy.data.CandyCommands
import it.tourtools.gandy.data.CandyNetwork
import it.tourtools.gandy.data.CandyRepository
import it.tourtools.gandy.data.model.WashingMachineStatus
import it.tourtools.gandy.data.toUiText
import it.tourtools.gandy.ui.common.UiText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val ip: String = "",
    val status: WashingMachineStatus? = null,
    val loading: Boolean = false,
    val error: UiText? = null,
    val selectedPr: Int = NO_SELECTION,
    val selectedPrCode: Int = NO_SELECTION,
    val selectedTemp: Int = NO_SELECTION,
    val selectedSpin: Int = NO_SELECTION,
    val cycleTotalTime: Int = 0,
) {
    companion object {
        const val NO_SELECTION = -1
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: CandyRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var pollingJob: Job? = null
    private var selectionsInitialized = false

    fun connect(ip: String) {
        if (ip == _state.value.ip) return
        selectionsInitialized = false
        _state.update { HomeUiState(ip = ip) }
        startPolling(ip)
    }

    fun setProgram(pr: Int, prCode: Int) {
        _state.update { it.copy(selectedPr = pr, selectedPrCode = prCode) }
    }

    fun setTemp(value: Int) {
        _state.update { it.copy(selectedTemp = value) }
    }

    fun setSpin(value: Int) {
        _state.update { it.copy(selectedSpin = value) }
    }

    fun refresh() {
        val ip = _state.value.ip
        if (ip.isNotBlank()) viewModelScope.launch { fetchStatus(ip, showError = true) }
    }

    fun startProgram() {
        val s = _state.value
        if (s.ip.isBlank() || s.selectedPrCode < 0) return
        sendCommand(
            CandyCommands.start(
                pr = s.selectedPr,
                prCode = s.selectedPrCode,
                temperature = s.selectedTemp,
                spin = s.selectedSpin,
            ),
        )
    }

    fun stopProgram() {
        val status = _state.value.status ?: return
        sendCommand(CandyCommands.stop(status.program))
    }

    fun pause() = sendCommand(CandyCommands.PAUSE)

    fun resume() = sendCommand(CandyCommands.RESUME)

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun sendCommand(params: Map<String, String>) {
        val ip = _state.value.ip
        if (ip.isBlank()) return
        viewModelScope.launch {
            try {
                repo.sendCommand(ip, params)
                delay(CandyNetwork.REFRESH_DELAY_AFTER_COMMAND_MS)
                fetchStatus(ip)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.toUiText()) }
            }
        }
    }

    private fun startPolling(ip: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                fetchStatus(ip)
                delay(CandyNetwork.POLL_INTERVAL_MS)
            }
        }
    }

    private suspend fun fetchStatus(ip: String, showError: Boolean = false) {
        _state.update { it.copy(loading = true) }
        try {
            val status = repo.readStatus(ip)
            _state.update { st ->
                val totalTime = when {
                    !status.isRunning && !status.isPaused -> 0
                    st.cycleTotalTime == 0 && status.remainingTime > 0 -> status.remainingTime
                    st.cycleTotalTime > 0 -> st.cycleTotalTime
                    else -> 0
                }
                val newState = st.copy(
                    status = status,
                    loading = false,
                    error = null,
                    cycleTotalTime = totalTime,
                )
                if (!selectionsInitialized && !status.isRunning) {
                    selectionsInitialized = true
                    newState.copy(
                        selectedPr = status.program,
                        selectedPrCode = status.programCode,
                        selectedTemp = status.temperature,
                        selectedSpin = status.spinSpeed,
                    )
                } else {
                    newState
                }
            }
        } catch (e: Exception) {
            val hasStatus = _state.value.status != null
            _state.update {
                it.copy(
                    loading = false,
                    error = if (hasStatus && !showError) it.error else e.toUiText(),
                )
            }
        }
    }
}
