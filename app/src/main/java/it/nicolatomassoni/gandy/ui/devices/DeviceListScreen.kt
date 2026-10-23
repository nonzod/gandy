package it.nicolatomassoni.gandy.ui.devices

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BugReport
import it.nicolatomassoni.gandy.BuildConfig
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.model.Appliance
import it.nicolatomassoni.gandy.data.model.ApplianceQrData
import it.nicolatomassoni.gandy.ui.components.GandyTopBar
import it.nicolatomassoni.gandy.ui.devices.components.AddDeviceDialog
import it.nicolatomassoni.gandy.ui.devices.components.DeviceList
import it.nicolatomassoni.gandy.ui.devices.components.EmptyDevicesContent
import it.nicolatomassoni.gandy.ui.devices.components.ShareQrDialog
import it.nicolatomassoni.gandy.ui.preview.PreviewData
import it.nicolatomassoni.gandy.ui.preview.ThemePreviews
import it.nicolatomassoni.gandy.ui.theme.GandyTheme
import kotlinx.serialization.json.Json

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
    val context = androidx.compose.ui.platform.LocalContext.current
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var shareAppliance by remember { mutableStateOf<Appliance?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { contents ->
            runCatching {
                val data = Json.decodeFromString<ApplianceQrData>(contents)
                if (data.v >= 1) onAdd(data.toAppliance())
            }
        }
    }

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
                    if (BuildConfig.DEBUG) {
                        IconButton(onClick = onDemo) {
                            Icon(Icons.Outlined.BugReport, contentDescription = "Demo")
                        }
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
                onShare = { shareAppliance = it },
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
            onScanQr = {
                showAddDialog = false
                scanLauncher.launch(
                    ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt(context.getString(R.string.qr_scan_prompt))
                        setBeepEnabled(false)
                        setCaptureActivity(PortraitCaptureActivity::class.java)
                    },
                )
            },
        )
    }

    shareAppliance?.let { appliance ->
        ShareQrDialog(
            appliance = appliance,
            onDismiss = { shareAppliance = null },
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
