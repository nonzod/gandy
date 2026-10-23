package it.nicolatomassoni.gandy.ui.control

import it.nicolatomassoni.gandy.data.model.WashingMachineStatus
import it.nicolatomassoni.gandy.ui.common.UiText

data class ControlUiState(
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

/** Azioni dell'utente sulla schermata di controllo. */
class ControlActions(
    val onRefresh: () -> Unit = {},
    val onStart: () -> Unit = {},
    val onStop: () -> Unit = {},
    val onPause: () -> Unit = {},
    val onResume: () -> Unit = {},
    val onSelectProgram: (pr: Int, prCode: Int) -> Unit = { _, _ -> },
    val onSelectTemp: (Int) -> Unit = {},
    val onSelectSpin: (Int) -> Unit = {},
    val onDismissError: () -> Unit = {},
)
