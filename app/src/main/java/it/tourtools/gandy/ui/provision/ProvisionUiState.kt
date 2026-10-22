package it.tourtools.gandy.ui.provision

import androidx.annotation.StringRes
import it.tourtools.gandy.R
import it.tourtools.gandy.ui.common.UiText
import java.io.Serializable

enum class ProvisionStep(@StringRes val labelRes: Int) {
    Instructions(R.string.provision_step_preparation),
    WifiCredentials(R.string.provision_step_wifi),
    SaveDevice(R.string.provision_step_save),
}

/**
 * Stato della procedura di configurazione. È [Serializable] per sopravvivere alla morte
 * del processo (l'utente esce dall'app per collegarsi al Wi-Fi del dispositivo);
 * caricamento ed errore sono transitori e non vengono salvati.
 */
data class ProvisionUiState(
    val step: ProvisionStep = ProvisionStep.Instructions,
    val ssid: String = "",
    val password: String = "",
    val deviceName: String = "",
    val deviceIp: String = "",
    val generatedKey: String = "",
    @Transient val loading: Boolean = false,
    @Transient val error: UiText? = null,
) : Serializable {

    val canSend: Boolean get() = ssid.isNotBlank() && password.isNotBlank() && !loading

    val canSave: Boolean get() = deviceName.isNotBlank() && deviceIp.isNotBlank()

    private companion object {
        private const val serialVersionUID = 1L
    }
}
