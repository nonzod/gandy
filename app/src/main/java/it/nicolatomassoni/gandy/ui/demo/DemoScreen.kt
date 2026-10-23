package it.nicolatomassoni.gandy.ui.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.nicolatomassoni.gandy.data.MachineMode
import it.nicolatomassoni.gandy.data.ProgramPhase
import it.nicolatomassoni.gandy.data.model.WashingMachineStatus
import it.nicolatomassoni.gandy.ui.components.ErrorCard
import it.nicolatomassoni.gandy.ui.components.GandyTopBar
import it.nicolatomassoni.gandy.ui.control.ControlActions
import it.nicolatomassoni.gandy.ui.control.ControlUiState
import it.nicolatomassoni.gandy.ui.control.components.ActionButtons
import it.nicolatomassoni.gandy.ui.control.components.DoneActions
import it.nicolatomassoni.gandy.ui.control.components.IdleCard
import it.nicolatomassoni.gandy.ui.control.components.PhaseIndicator
import it.nicolatomassoni.gandy.ui.control.components.ProgramControls
import it.nicolatomassoni.gandy.ui.control.components.RemoteWarningCard
import it.nicolatomassoni.gandy.ui.control.components.RunningCard
import it.nicolatomassoni.gandy.ui.provision.ProvisionStep
import it.nicolatomassoni.gandy.ui.provision.components.InstructionsStep
import it.nicolatomassoni.gandy.ui.provision.components.SaveDeviceStep
import it.nicolatomassoni.gandy.ui.provision.components.StepIndicator
import it.nicolatomassoni.gandy.ui.provision.components.WifiCredentialsStep

private enum class DemoMode(val label: String) {
    LOADING("Caricamento"),
    REMOTE_OFF("Remoto off"),
    IDLE("Pronta"),
    RUNNING_PREWASH("Prelavaggio"),
    RUNNING_WASH("Lavaggio"),
    RUNNING_RINSE("Risciacquo"),
    RUNNING_SPIN("Centrifuga"),
    PAUSED("In pausa"),
    DONE("Terminato"),
    ERROR("Errore"),
    PROVISION_STEP1("Preparazione"),
    PROVISION_STEP2("Rete Wi-Fi"),
    PROVISION_STEP3("Salvataggio"),
}

private val BASE_STATUS = WashingMachineStatus(
    machineMode = MachineMode.READY,
    program = 2,
    programCode = 2,
    temperature = 40,
    spinSpeed = 12,
    wifiStatus = 1,
)

private const val CYCLE_TOTAL = 90 * 60

private val NO_OP_ACTIONS = ControlActions()

@Composable
fun DemoScreen(onBack: () -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(2) }
    val modes = DemoMode.entries

    Scaffold(
        topBar = {
            GandyTopBar(
                title = { Text("Demo", fontWeight = FontWeight.SemiBold) },
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(modes) { index, mode ->
                    FilterChip(
                        selected = index == selected,
                        onClick = { selected = index },
                        label = { Text(mode.label) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(4.dp))
                DemoContent(modes[selected])
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DemoContent(mode: DemoMode) {
    when (mode) {
        DemoMode.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        DemoMode.REMOTE_OFF -> {
            RemoteWarningCard()
            IdleCard(BASE_STATUS.copy(wifiStatus = 0), loading = false)
        }

        DemoMode.IDLE -> {
            IdleCard(BASE_STATUS, loading = false)
            ProgramControls(
                state = ControlUiState(
                    status = BASE_STATUS,
                    selectedPr = 2,
                    selectedPrCode = 2,
                    selectedTemp = 40,
                    selectedSpin = 12,
                ),
                actions = NO_OP_ACTIONS,
            )
        }

        DemoMode.RUNNING_PREWASH,
        DemoMode.RUNNING_WASH,
        DemoMode.RUNNING_RINSE,
        DemoMode.RUNNING_SPIN,
        -> {
            val (phase, remaining) = when (mode) {
                DemoMode.RUNNING_PREWASH -> ProgramPhase.PREWASH to 83 * 60
                DemoMode.RUNNING_WASH -> ProgramPhase.WASH to 62 * 60
                DemoMode.RUNNING_RINSE -> ProgramPhase.RINSE to 25 * 60
                else -> ProgramPhase.SPIN.first to 8 * 60
            }
            val status = BASE_STATUS.copy(
                machineMode = MachineMode.RUNNING,
                programPhase = phase,
                remainingTime = remaining,
            )
            RunningCard(status, loading = false, cycleTotalTime = CYCLE_TOTAL)
            PhaseIndicator(status.programPhase)
            ActionButtons(isPaused = false, onPause = {}, onResume = {}, onStop = {})
        }

        DemoMode.PAUSED -> {
            val status = BASE_STATUS.copy(
                machineMode = MachineMode.PAUSED,
                programPhase = ProgramPhase.WASH,
                remainingTime = 62 * 60,
            )
            RunningCard(status, loading = false, cycleTotalTime = CYCLE_TOTAL)
            PhaseIndicator(status.programPhase)
            ActionButtons(isPaused = true, onPause = {}, onResume = {}, onStop = {})
        }

        DemoMode.DONE -> {
            val status = BASE_STATUS.copy(
                machineMode = MachineMode.DONE.first,
                programPhase = ProgramPhase.END,
            )
            IdleCard(status, loading = false)
            DoneActions(onReset = {})
        }

        DemoMode.ERROR -> {
            val status = BASE_STATUS.copy(
                machineMode = MachineMode.ERROR,
                error = 3,
            )
            IdleCard(status, loading = false)
            ErrorCard("Errore di comunicazione con la lavatrice", onDismiss = {})
        }

        DemoMode.PROVISION_STEP1 -> {
            StepIndicator(current = ProvisionStep.Instructions)
            InstructionsStep(onNext = {})
        }

        DemoMode.PROVISION_STEP2 -> {
            StepIndicator(current = ProvisionStep.WifiCredentials)
            WifiCredentialsStep(
                ssid = "CasaMia_WiFi",
                password = "password123",
                onSsidChange = {},
                onPasswordChange = {},
                loading = false,
                error = null,
                onSend = {},
            )
        }

        DemoMode.PROVISION_STEP3 -> {
            StepIndicator(current = ProvisionStep.SaveDevice)
            SaveDeviceStep(
                generatedKey = "aB3dE5fG7hI9jK1l",
                deviceName = "Lavatrice",
                deviceIp = "192.168.1.100",
                onNameChange = {},
                onIpChange = {},
                onComplete = {},
            )
        }
    }
}
