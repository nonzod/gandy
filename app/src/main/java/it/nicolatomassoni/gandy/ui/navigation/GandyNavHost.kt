package it.nicolatomassoni.gandy.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import it.nicolatomassoni.gandy.ui.control.ControlRoute
import it.nicolatomassoni.gandy.ui.demo.DemoScreen
import it.nicolatomassoni.gandy.ui.devices.DeviceListRoute
import it.nicolatomassoni.gandy.ui.provision.ProvisionRoute

private enum class Screen { List, Control, Provision, Demo }

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
            onDemo = { screen = Screen.Demo },
        )
        Screen.Control -> ControlRoute(
            ip = selectedIp,
            deviceName = selectedName,
            onBack = { screen = Screen.List },
        )
        Screen.Provision -> ProvisionRoute(
            onBack = { screen = Screen.List },
        )
        Screen.Demo -> DemoScreen(
            onBack = { screen = Screen.List },
        )
    }
}
