package it.tourtools.gandy.ui.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.tourtools.gandy.R
import it.tourtools.gandy.data.model.WashingMachineStatus
import it.tourtools.gandy.ui.components.ErrorCard
import it.tourtools.gandy.ui.components.GandyTopBar
import it.tourtools.gandy.ui.control.components.ActionButtons
import it.tourtools.gandy.ui.control.components.DoneActions
import it.tourtools.gandy.ui.control.components.IdleCard
import it.tourtools.gandy.ui.control.components.PhaseIndicator
import it.tourtools.gandy.ui.control.components.ProgramControls
import it.tourtools.gandy.ui.control.components.RemoteWarningCard
import it.tourtools.gandy.ui.control.components.RunningCard
import it.tourtools.gandy.ui.preview.PreviewData
import it.tourtools.gandy.ui.preview.ThemePreviews
import it.tourtools.gandy.ui.theme.GandyTheme

/** Punto d'ingresso con ViewModel: collega lo stato alla UI stateless [ControlScreen]. */
@Composable
fun ControlRoute(
    ip: String,
    deviceName: String,
    onBack: () -> Unit,
    viewModel: ControlViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(ip) { viewModel.connect(ip) }

    val actions = remember(viewModel) {
        ControlActions(
            onRefresh = viewModel::refresh,
            onStart = viewModel::startProgram,
            onStop = viewModel::stopProgram,
            onPause = viewModel::pause,
            onResume = viewModel::resume,
            onSelectProgram = viewModel::setProgram,
            onSelectTemp = viewModel::setTemp,
            onSelectSpin = viewModel::setSpin,
            onDismissError = viewModel::clearError,
        )
    }

    ControlScreen(
        deviceName = deviceName,
        state = state,
        actions = actions,
        onBack = onBack,
    )
}

@Composable
fun ControlScreen(
    deviceName: String,
    state: ControlUiState,
    actions: ControlActions,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            GandyTopBar(
                title = { Text(deviceName, fontWeight = FontWeight.SemiBold) },
                onBack = onBack,
                actions = {
                    IconButton(onClick = actions.onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            StatusContent(state, actions)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusContent(state: ControlUiState, actions: ControlActions) {
    val status = state.status

    if (state.loading && status == null) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 120.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (status != null) {
        if (!status.remoteReady) {
            RemoteWarningCard()
        }

        if (status.isRunning || status.isPaused) {
            RunningCard(status, state.loading, state.cycleTotalTime)
            PhaseIndicator(status.programPhase)
            ActionButtons(
                isPaused = status.isPaused,
                onPause = actions.onPause,
                onResume = actions.onResume,
                onStop = actions.onStop,
            )
        } else {
            IdleCard(status, state.loading)
            if (status.isDone) {
                DoneActions(onReset = actions.onStop)
            } else if (status.remoteReady) {
                ProgramControls(state, actions)
            }
        }
    }

    state.error?.let { error ->
        ErrorCard(error.asString(), onDismiss = actions.onDismissError)
    }
}

private class ControlStatusProvider : PreviewParameterProvider<WashingMachineStatus> {
    override val values = sequenceOf(
        PreviewData.idleStatus,
        PreviewData.runningStatus,
        PreviewData.pausedStatus,
        PreviewData.doneStatus,
        PreviewData.errorStatus,
        PreviewData.remoteDisabledStatus,
    )
}

@ThemePreviews
@Composable
private fun ControlScreenPreview(
    @PreviewParameter(ControlStatusProvider::class) status: WashingMachineStatus,
) {
    GandyTheme {
        ControlScreen(
            deviceName = "Lavatrice",
            state = ControlUiState(
                status = status,
                selectedPr = status.program,
                selectedPrCode = status.programCode,
                selectedTemp = status.temperature,
                selectedSpin = status.spinSpeed,
                cycleTotalTime = 90 * 60,
            ),
            actions = ControlActions(),
            onBack = {},
        )
    }
}
