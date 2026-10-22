package it.tourtools.gandy.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.tourtools.gandy.R
import it.tourtools.gandy.data.toUiText
import it.tourtools.gandy.ui.common.UiText
import it.tourtools.gandy.ui.components.GandyTopBar
import it.tourtools.gandy.ui.components.NumberedStep
import kotlinx.coroutines.launch

@Composable
fun ProvisionScreen(
    onBack: () -> Unit,
    onProvision: suspend (ssid: String, password: String) -> Result<String>,
    onAddDevice: (name: String, ip: String, xorKey: String) -> Unit,
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var ssid by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var deviceName by rememberSaveable { mutableStateOf("") }
    var deviceIp by rememberSaveable { mutableStateOf("") }
    var loading by rememberSaveable { mutableStateOf(false) }
    var generatedKey by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            GandyTopBar(
                title = { Text(stringResource(R.string.provision_title), fontWeight = FontWeight.SemiBold) },
                onBack = onBack,
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

            StepIndicator(current = step)

            when (step) {
                0 -> InstructionsStep(onNext = { step = 1 })
                1 -> WifiCredentialsStep(
                    ssid = ssid,
                    password = password,
                    onSsidChange = { ssid = it },
                    onPasswordChange = { password = it },
                    loading = loading,
                    error = error,
                    onSend = {
                        loading = true
                        error = null
                    },
                    onResult = { key ->
                        loading = false
                        generatedKey = key
                        step = 2
                    },
                    onError = { msg ->
                        loading = false
                        error = msg
                    },
                    onProvision = onProvision,
                )
                2 -> FinalStep(
                    generatedKey = generatedKey,
                    deviceName = deviceName,
                    deviceIp = deviceIp,
                    onNameChange = { deviceName = it },
                    onIpChange = { deviceIp = it },
                    onComplete = {
                        onAddDevice(deviceName.trim(), deviceIp.trim(), generatedKey)
                        onBack()
                    },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepIndicator(current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        STEP_LABELS.forEachIndexed { index, labelRes ->
            val isActive = index <= current
            val isDone = index < current
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isDone) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun InstructionsStep(onNext: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.provision_step_preparation),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            NumberedStep(1, stringResource(R.string.provision_instruction_1))
            NumberedStep(2, stringResource(R.string.provision_instruction_2))
            NumberedStep(3, stringResource(R.string.provision_instruction_3))
            NumberedStep(4, stringResource(R.string.provision_instruction_4))
        }
    }

    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(stringResource(R.string.provision_connected), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun WifiCredentialsStep(
    ssid: String,
    password: String,
    onSsidChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    loading: Boolean,
    error: String?,
    onSend: () -> Unit,
    onResult: (key: String) -> Unit,
    onError: (String) -> Unit,
    onProvision: suspend (String, String) -> Result<String>,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.provision_wifi_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.provision_wifi_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = ssid,
                onValueChange = onSsidChange,
                label = { Text(stringResource(R.string.provision_ssid)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.provision_password)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    error?.let {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                it,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    Button(
        onClick = {
            onSend()
            scope.launch {
                onProvision(ssid.trim(), password).fold(
                    onSuccess = { key -> onResult(key) },
                    onFailure = { e -> onError((e.toUiText() ?: UiText.Resource(R.string.error_unknown)).asString(context)) },
                )
            }
        },
        enabled = ssid.isNotBlank() && password.isNotBlank() && !loading,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(stringResource(R.string.provision_send), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun FinalStep(
    generatedKey: String,
    deviceName: String,
    deviceIp: String,
    onNameChange: (String) -> Unit,
    onIpChange: (String) -> Unit,
    onComplete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.provision_sent_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.provision_sent_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            NumberedStep(1, stringResource(R.string.provision_final_1))
            NumberedStep(2, stringResource(R.string.provision_final_2))
            NumberedStep(3, stringResource(R.string.provision_final_3))
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.provision_key),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    generatedKey,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            OutlinedTextField(
                value = deviceName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.provision_device_name)) },
                placeholder = { Text(stringResource(R.string.placeholder_device_name)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = deviceIp,
                onValueChange = onIpChange,
                label = { Text(stringResource(R.string.label_ip_address)) },
                placeholder = { Text(stringResource(R.string.placeholder_ip_address)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    Button(
        onClick = onComplete,
        enabled = deviceName.isNotBlank() && deviceIp.isNotBlank(),
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(stringResource(R.string.provision_save), style = MaterialTheme.typography.titleMedium)
    }
}

private val STEP_LABELS = listOf(
    R.string.provision_step_preparation,
    R.string.provision_step_wifi,
    R.string.provision_step_save,
)
