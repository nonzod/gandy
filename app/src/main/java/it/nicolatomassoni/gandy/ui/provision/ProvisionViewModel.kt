package it.nicolatomassoni.gandy.ui.provision

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.CandyNetwork
import it.nicolatomassoni.gandy.data.CandyProtocol
import it.nicolatomassoni.gandy.data.CandyRepository
import it.nicolatomassoni.gandy.data.model.Appliance
import it.nicolatomassoni.gandy.data.toUiText
import it.nicolatomassoni.gandy.ui.common.UiText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProvisionViewModel @Inject constructor(
    private val repo: CandyRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(savedStateHandle[KEY_STATE] ?: ProvisionUiState())
    val state: StateFlow<ProvisionUiState> = _state.asStateFlow()

    private var provisionJob: Job? = null

    fun onDeviceConnected() = updateState { it.copy(step = ProvisionStep.WifiCredentials) }

    fun onSsidChange(value: String) = updateState { it.copy(ssid = value) }

    fun onPasswordChange(value: String) = updateState { it.copy(password = value) }

    fun onDeviceNameChange(value: String) = updateState { it.copy(deviceName = value) }

    fun onDeviceIpChange(value: String) = updateState { it.copy(deviceIp = value) }

    /** Invia le credenziali Wi-Fi al dispositivo con una chiave XOR appena generata. */
    fun sendConfiguration() {
        val s = _state.value
        if (!s.canSend) return
        updateState { it.copy(loading = true, error = null) }
        provisionJob = viewModelScope.launch {
            val key = generateKey()
            try {
                repo.provision(CandyNetwork.PROVISIONING_IP, s.ssid.trim(), s.password, key)
                updateState { it.copy(loading = false, generatedKey = key, step = ProvisionStep.SaveDevice) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateState {
                    it.copy(loading = false, error = e.toUiText() ?: UiText.Resource(R.string.error_unknown))
                }
            }
        }
    }

    /** Salva il dispositivo configurato e azzera la procedura. */
    fun saveDevice() {
        val s = _state.value
        val appliance = Appliance(name = s.deviceName.trim(), ip = s.deviceIp.trim(), xorKey = s.generatedKey)
        viewModelScope.launch { repo.addAppliance(appliance) }
        reset()
    }

    /**
     * Azzera la procedura. Il ViewModel vive quanto l'Activity, quindi va chiamato
     * all'uscita dalla schermata per ripartire dal primo passo alla prossima apertura.
     */
    fun reset() {
        provisionJob?.cancel()
        provisionJob = null
        updateState { ProvisionUiState() }
    }

    private inline fun updateState(transform: (ProvisionUiState) -> ProvisionUiState) {
        _state.update(transform)
        savedStateHandle[KEY_STATE] = _state.value
    }

    private fun generateKey(): String = buildString {
        repeat(CandyProtocol.XOR_KEY_LENGTH) { append(CandyProtocol.XOR_KEY_CHARSET.random()) }
    }

    private companion object {
        const val KEY_STATE = "provision_state"
    }
}
