package it.tourtools.gandy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.tourtools.gandy.data.model.Appliance
import it.tourtools.gandy.ui.devices.DeviceListScreen
import it.tourtools.gandy.ui.devices.DevicesViewModel
import it.tourtools.gandy.ui.devices.ProvisionScreen
import it.tourtools.gandy.ui.home.HomeScreen

private enum class Screen { List, Control, Provision }

@Composable
fun GandyNavHost(devicesVm: DevicesViewModel = hiltViewModel()) {
    val appliances by devicesVm.appliances.collectAsStateWithLifecycle()

    var screen by rememberSaveable { mutableStateOf(Screen.List) }
    var selectedIp by rememberSaveable { mutableStateOf("") }
    var selectedName by rememberSaveable { mutableStateOf("") }

    when (screen) {
        Screen.List -> DeviceListScreen(
            appliances = appliances,
            onSelect = { a ->
                selectedIp = a.ip
                selectedName = a.name
                screen = Screen.Control
            },
            onAdd = devicesVm::addAppliance,
            onRemove = devicesVm::removeAppliance,
            onProvision = { screen = Screen.Provision },
        )
        Screen.Control -> HomeScreen(
            ip = selectedIp,
            deviceName = selectedName,
            onBack = { screen = Screen.List },
        )
        Screen.Provision -> ProvisionScreen(
            onBack = { screen = Screen.List },
            onProvision = devicesVm::provision,
            onAddDevice = { name, ip, xorKey ->
                devicesVm.addAppliance(Appliance(name = name, ip = ip, xorKey = xorKey))
            },
        )
    }
}
