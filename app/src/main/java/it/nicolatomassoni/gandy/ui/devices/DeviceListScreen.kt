package it.nicolatomassoni.gandy.ui.devices

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.model.Appliance
import it.nicolatomassoni.gandy.ui.components.GandyTopBar
import it.nicolatomassoni.gandy.ui.devices.components.AddDeviceDialog
import it.nicolatomassoni.gandy.ui.devices.components.DeviceList
import it.nicolatomassoni.gandy.ui.devices.components.EmptyDevicesContent
import it.nicolatomassoni.gandy.ui.preview.PreviewData
import it.nicolatomassoni.gandy.ui.preview.ThemePreviews
import it.nicolatomassoni.gandy.ui.theme.GandyTheme

/** Punto d'ingresso con ViewModel: collega lo stato alla UI stateless [DeviceListScreen]. */
@Composable
fun DeviceListRoute(
    onSelect: (Appliance) -> Unit,
    onProvision: () -> Unit,
    onDemo: () -> Unit,
    viewModel: DevicesViewModel = hiltViewModel(),
) {
    val appliances by viewModel.appliances.collectAsStateWithLifecycle()

    DeviceListScreen(
        appliances = appliances,
        onSelect = onSelect,
        onAdd = viewModel::addAppliance,
        onRemove = viewModel::removeAppliance,
        onProvision = onProvision,
        onDemo = onDemo,
    )
}

@Composable
fun DeviceListScreen(
    appliances: List<Appliance>,
    onSelect: (Appliance) -> Unit,
    onAdd: (Appliance) -> Unit,
    onRemove: (String) -> Unit,
    onProvision: () -> Unit,
    onDemo: () -> Unit = {},
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GandyTopBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                actions = {
                    IconButton(onClick = onDemo) {
                        Icon(Icons.Outlined.BugReport, contentDescription = "Demo")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
            }
        },
    ) { padding ->
        if (appliances.isEmpty()) {
            EmptyDevicesContent(
                onAdd = { showAddDialog = true },
                onProvision = onProvision,
                modifier = Modifier.padding(padding),
            )
        } else {
            DeviceList(
                appliances = appliances,
                onSelect = onSelect,
                onRemove = onRemove,
                onProvision = onProvision,
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (showAddDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, ip ->
                onAdd(Appliance(name = name, ip = ip))
                showAddDialog = false
            },
        )
    }
}

@ThemePreviews
@Composable
private fun DeviceListScreenPreview() {
    GandyTheme {
        DeviceListScreen(
            appliances = PreviewData.appliances,
            onSelect = {},
            onAdd = {},
            onRemove = {},
            onProvision = {},
        )
    }
}

@ThemePreviews
@Composable
private fun DeviceListScreenEmptyPreview() {
    GandyTheme {
        DeviceListScreen(
            appliances = emptyList(),
            onSelect = {},
            onAdd = {},
            onRemove = {},
            onProvision = {},
        )
    }
}
