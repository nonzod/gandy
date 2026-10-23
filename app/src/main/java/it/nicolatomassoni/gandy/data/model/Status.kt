package it.nicolatomassoni.gandy.data.model

import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.CandyProtocol
import it.nicolatomassoni.gandy.data.MachineMode
import it.nicolatomassoni.gandy.data.ProgramPhase
import it.nicolatomassoni.gandy.data.WashPrograms
import it.nicolatomassoni.gandy.ui.common.UiText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApplianceResponse(
    val statusLavatrice: WashingMachineStatus? = null,
)

@Serializable
data class WashingMachineStatus(
    @SerialName("MachMd") val machineMode: Int = 0,
    @SerialName("Pr") val program: Int = 0,
    @SerialName("PrPh") val programPhase: Int = 0,
    @SerialName("PrCode") val programCode: Int = 0,
    @SerialName("Temp") val temperature: Int = 0,
    @SerialName("SpinSp") val spinSpeed: Int = 0,
    @SerialName("SLevel") val soilLevel: Int = 0,
    @SerialName("RemTime") val remainingTime: Int = 0,
    @SerialName("Err") val error: Int = 0,
    @SerialName("WiFiStatus") val wifiStatus: Int = 0,
) {
    val remoteReady: Boolean get() = wifiStatus == CandyProtocol.WIFI_STATUS_REMOTE_READY

    val machineModeLabel: UiText
        get() = MachineMode.labelRes(machineMode)?.let { UiText.Resource(it) }
            ?: UiText.Resource(R.string.mode_unknown, machineMode)

    val programLabel: UiText
        get() = WashPrograms.labelRes(programCode)?.let { UiText.Resource(it) }
            ?: UiText.Resource(R.string.program_unknown, program)

    val programPhaseLabel: UiText
        get() = ProgramPhase.labelRes(programPhase)?.let { UiText.Resource(it) }
            ?: UiText.Resource(R.string.phase_unknown, programPhase)

    val spinRpm: Int
        get() = if (spinSpeed < CandyProtocol.SPIN_HUNDREDS_THRESHOLD) {
            spinSpeed * CandyProtocol.SPIN_MULTIPLIER
        } else {
            spinSpeed
        }

    val remainingMinutes: Int get() = remainingTime / SECONDS_PER_MINUTE

    val remainingFormatted: UiText
        get() {
            val h = remainingMinutes / MINUTES_PER_HOUR
            val m = remainingMinutes % MINUTES_PER_HOUR
            return if (h > 0) {
                UiText.Resource(R.string.remaining_hours_minutes, h, m)
            } else {
                UiText.Resource(R.string.remaining_minutes, m)
            }
        }

    val isRunning: Boolean get() = machineMode == MachineMode.RUNNING

    val isPaused: Boolean get() = machineMode == MachineMode.PAUSED

    val isDelayed: Boolean get() = machineMode in MachineMode.DELAYED

    val isDone: Boolean get() = machineMode in MachineMode.DONE

    val hasError: Boolean get() = machineMode == MachineMode.ERROR || error != 0

    private companion object {
        const val SECONDS_PER_MINUTE = 60
        const val MINUTES_PER_HOUR = 60
    }
}
