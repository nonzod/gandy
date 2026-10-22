package it.tourtools.gandy.ui.preview

import it.tourtools.gandy.data.MachineMode
import it.tourtools.gandy.data.ProgramPhase
import it.tourtools.gandy.data.model.Appliance
import it.tourtools.gandy.data.model.WashingMachineStatus

/** Dati fittizi usati solo dalle @Preview. */
internal object PreviewData {

    val idleStatus = WashingMachineStatus(
        machineMode = MachineMode.READY,
        program = 2,
        programCode = 2,
        temperature = 40,
        spinSpeed = 12,
        wifiStatus = 1,
    )

    val runningStatus = idleStatus.copy(
        machineMode = MachineMode.RUNNING,
        programPhase = ProgramPhase.RINSE,
        remainingTime = 47 * 60,
    )

    val pausedStatus = runningStatus.copy(machineMode = MachineMode.PAUSED)

    val doneStatus = idleStatus.copy(machineMode = MachineMode.DONE.first, programPhase = ProgramPhase.END)

    val errorStatus = idleStatus.copy(machineMode = MachineMode.ERROR, error = 3)

    val remoteDisabledStatus = idleStatus.copy(wifiStatus = 0)

    val appliances = listOf(
        Appliance(id = "1", name = "Lavatrice", ip = "192.168.1.100"),
        Appliance(id = "2", name = "Lavatrice cantina", ip = "192.168.1.101"),
    )
}
