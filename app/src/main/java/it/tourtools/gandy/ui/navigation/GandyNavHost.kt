package it.tourtools.gandy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import it.tourtools.gandy.ui.control.ControlRoute
import it.tourtools.gandy.ui.devices.DeviceListRoute
import it.tourtools.gandy.ui.provision.ProvisionRoute

private enum class Screen { List, Control, Provision }

@Composable
fun GandyNavHost() {
    var screen by rememberSaveable { mutableStateOf(Screen.List) }
    var selectedIp by rememberSaveable { mutableStateOf("") }
    var selectedName by rememberSaveable { mutableStateOf("") }

    when (screen) {
        Screen.List -> DeviceListRoute(
            onSelect = { a ->
                selectedIp = a.ip
                selectedName = a.name
                screen = Screen.Control
            },
            onProvision = { screen = Screen.Provision },
        )
        Screen.Control -> ControlRoute(
            ip = selectedIp,
            deviceName = selectedName,
            onBack = { screen = Screen.List },
        )
        Screen.Provision -> ProvisionRoute(
            onBack = { screen = Screen.List },
        )
    }
}
