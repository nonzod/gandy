package it.nicolatomassoni.gandy.ui.provision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.ui.components.GandyTopBar
import it.nicolatomassoni.gandy.ui.preview.ThemePreviews
import it.nicolatomassoni.gandy.ui.provision.components.InstructionsStep
import it.nicolatomassoni.gandy.ui.provision.components.SaveDeviceStep
import it.nicolatomassoni.gandy.ui.provision.components.StepIndicator
import it.nicolatomassoni.gandy.ui.provision.components.WifiCredentialsStep
import it.nicolatomassoni.gandy.ui.theme.GandyTheme

/** Azioni dell'utente sulla schermata di configurazione. */
class ProvisionActions(
    val onBack: () -> Unit = {},
    val onDeviceConnected: () -> Unit = {},
    val onSsidChange: (String) -> Unit = {},
    val onPasswordChange: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onDeviceNameChange: (String) -> Unit = {},
    val onDeviceIpChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
)

/** Punto d'ingresso con ViewModel: collega lo stato alla UI stateless [ProvisionScreen]. */
@Composable
fun ProvisionRoute(
    onBack: () -> Unit,
    viewModel: ProvisionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val actions = remember(viewModel, onBack) {
        ProvisionActions(
            onBack = {
                viewModel.reset()
                onBack()
            },
            onDeviceConnected = viewModel::onDeviceConnected,
            onSsidChange = viewModel::onSsidChange,
            onPasswordChange = viewModel::onPasswordChange,
            onSend = viewModel::sendConfiguration,
            onDeviceNameChange = viewModel::onDeviceNameChange,
            onDeviceIpChange = viewModel::onDeviceIpChange,
            onSave = {
                viewModel.saveDevice()
                onBack()
            },
        )
    }

    ProvisionScreen(state = state, actions = actions)
}

@Composable
fun ProvisionScreen(
    state: ProvisionUiState,
    actions: ProvisionActions,
) {
    Scaffold(
        topBar = {
            GandyTopBar(
                title = { Text(stringResource(R.string.provision_title), fontWeight = FontWeight.SemiBold) },
                onBack = actions.onBack,
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

            StepIndicator(current = state.step)

            when (state.step) {
                ProvisionStep.Instructions -> InstructionsStep(onNext = actions.onDeviceConnected)
                ProvisionStep.WifiCredentials -> WifiCredentialsStep(
                    ssid = state.ssid,
                    password = state.password,
                    onSsidChange = actions.onSsidChange,
                    onPasswordChange = actions.onPasswordChange,
                    loading = state.loading,
                    error = state.error?.asString(),
                    onSend = actions.onSend,
                )
                ProvisionStep.SaveDevice -> SaveDeviceStep(
                    generatedKey = state.generatedKey,
                    deviceName = state.deviceName,
                    deviceIp = state.deviceIp,
                    onNameChange = actions.onDeviceNameChange,
                    onIpChange = actions.onDeviceIpChange,
                    onComplete = actions.onSave,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private class ProvisionStateProvider : PreviewParameterProvider<ProvisionUiState> {
    override val values = sequenceOf(
        ProvisionUiState(),
        ProvisionUiState(step = ProvisionStep.WifiCredentials, ssid = "CasaMia", password = "password"),
        ProvisionUiState(step = ProvisionStep.WifiCredentials, ssid = "CasaMia", password = "password", loading = true),
        ProvisionUiState(step = ProvisionStep.SaveDevice, generatedKey = "aB3dE5fG7hI9jK1l"),
    )
}

@ThemePreviews
@Composable
private fun ProvisionScreenPreview(
    @PreviewParameter(ProvisionStateProvider::class) state: ProvisionUiState,
) {
    GandyTheme {
        ProvisionScreen(state = state, actions = ProvisionActions())
    }
}
