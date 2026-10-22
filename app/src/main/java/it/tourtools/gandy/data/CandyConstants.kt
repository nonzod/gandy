package it.tourtools.gandy.data

import androidx.annotation.StringRes
import it.tourtools.gandy.R
import it.tourtools.gandy.ui.common.UiText

/** Programma selezionabile: [pr] è il numero manopola (PrNm), [prCode] il codice inviato (PrCode). */
data class WashProgram(val pr: Int, val prCode: Int, @StringRes val labelRes: Int)

/** Valore selezionabile (temperatura, centrifuga) con la sua etichetta. */
data class SelectableOption(val value: Int, val label: UiText)

/** Parametri di rete e temporizzazioni. */
object CandyNetwork {
    const val CONNECT_TIMEOUT_SECONDS = 5L
    const val READ_TIMEOUT_SECONDS = 10L
    const val POLL_INTERVAL_MS = 60_000L
    const val REFRESH_DELAY_AFTER_COMMAND_MS = 1_000L

    /** IP dell'access point esposto dalla lavatrice in modalità configurazione. */
    const val PROVISIONING_IP = "192.168.0.1"

    const val DEFAULT_APPLIANCE_NAME = "Lavatrice"
}

/** Endpoint, parametri e valori del protocollo HTTP locale SimplyFi. */
object CandyProtocol {
    const val PATH_READ = "http-read.json"
    const val PATH_WRITE = "http-write.json"
    const val PATH_CONFIG = "http-config.json"

    const val QUERY_ENCRYPTED = "encrypted"
    const val QUERY_DATA = "data"

    /** Modalità di rete per http-config: client della rete domestica. */
    const val NETWORK_MODE_STATION = 3

    const val XOR_KEY_LENGTH = 16
    const val XOR_KEY_CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

    /** Prefissi in chiaro attesi della risposta cifrata, usati per ricavare la chiave XOR. */
    val STATUS_PLAINTEXT_PREFIXES = listOf(
        "{\r\n\t\"statusLavatrice\"",
        "{\"statusLavatrice\"",
    )

    /** WiFiStatus che indica manopola su Wi-Fi (controllo remoto abilitato). */
    const val WIFI_STATUS_REMOTE_READY = 1

    /** Valori di spinSpeed sotto questa soglia sono espressi in centinaia di rpm. */
    const val SPIN_HUNDREDS_THRESHOLD = 100
    const val SPIN_MULTIPLIER = 100

    object Param {
        const val WRITE = "Write"
        const val START_STOP = "StSt"
        const val PROGRAM_NUMBER = "PrNm"
        const val PROGRAM_CODE = "PrCode"
        const val TARGET_TEMPERATURE = "TmpTgt"
        const val TARGET_SPIN = "SpdTgt"
        const val TARGET_SOIL_LEVEL = "SLevTgt"
        const val OPTION_MASK_1 = "OptMsk1"
        const val OPTION_MASK_2 = "OptMsk2"
        const val STEAM = "Stm"
        const val DELAY = "DelVl"
        const val PAUSE = "Pa"

        const val NETWORK_MODE = "NTW_MODE"
        const val SSID = "SSID"
        const val PASSWORD = "PASSWORD"
        const val ENCRYPT_KEY = "ENCRYPT_KEY"
    }

    const val DEFAULT_SOIL_LEVEL = 3
}

/** Comandi pronti da inviare a http-write. L'ordine dei parametri è preservato. */
object CandyCommands {
    private const val ON = "1"
    private const val OFF = "0"

    fun start(pr: Int, prCode: Int, temperature: Int, spin: Int): Map<String, String> = with(CandyProtocol.Param) {
        mapOf(
            WRITE to ON,
            START_STOP to ON,
            PROGRAM_NUMBER to "$pr",
            PROGRAM_CODE to "$prCode",
            TARGET_TEMPERATURE to "$temperature",
            TARGET_SPIN to "$spin",
            TARGET_SOIL_LEVEL to "${CandyProtocol.DEFAULT_SOIL_LEVEL}",
            OPTION_MASK_1 to OFF,
            OPTION_MASK_2 to OFF,
            STEAM to OFF,
        )
    }

    fun stop(pr: Int): Map<String, String> = with(CandyProtocol.Param) {
        mapOf(
            WRITE to ON,
            START_STOP to OFF,
            PROGRAM_NUMBER to "$pr",
            DELAY to OFF,
        )
    }

    val PAUSE: Map<String, String> = mapOf(CandyProtocol.Param.PAUSE to ON)
    val RESUME: Map<String, String> = mapOf(CandyProtocol.Param.PAUSE to OFF)
}

/** Valori di MachMd. */
object MachineMode {
    const val READY = 1
    const val RUNNING = 2
    const val PAUSED = 3
    val DELAYED = 4..5
    const val ERROR = 6
    val DONE = 7..8

    @StringRes
    fun labelRes(mode: Int): Int? = when (mode) {
        READY -> R.string.mode_ready
        RUNNING -> R.string.mode_running
        PAUSED -> R.string.mode_paused
        in DELAYED -> R.string.mode_delayed
        ERROR -> R.string.mode_error
        in DONE -> R.string.mode_done
        else -> null
    }
}

/** Valori di PrPh. */
object ProgramPhase {
    const val READY = 0
    const val PREWASH = 1
    const val WASH = 2
    const val RINSE = 3
    const val LAST_RINSE = 4
    const val END = 5
    const val DRYING = 6
    const val ERROR = 7
    const val STEAM = 8
    val SPIN = 9..10

    @StringRes
    fun labelRes(phase: Int): Int? = when (phase) {
        READY -> R.string.phase_ready
        PREWASH -> R.string.phase_prewash
        WASH -> R.string.phase_wash
        RINSE -> R.string.phase_rinse
        LAST_RINSE -> R.string.phase_last_rinse
        END -> R.string.phase_end
        DRYING -> R.string.phase_drying
        ERROR -> R.string.phase_error
        STEAM -> R.string.phase_steam
        in SPIN -> R.string.phase_spin
        else -> null
    }

    /** Fasi mostrate nell'indicatore di avanzamento del ciclo. */
    val CYCLE_PHASES: List<Pair<Int, Int>> = listOf(
        PREWASH to R.string.phase_short_prewash,
        WASH to R.string.phase_wash,
        RINSE to R.string.phase_rinse,
        SPIN.first to R.string.phase_spin,
        END to R.string.phase_end,
    )
}

/** Programmi (PrCode) per la BWM4 137PH6/1-S. */
object WashPrograms {

    /** Etichette per tutti i PrCode noti, anche quelli non avviabili dall'app. */
    private val LABELS: Map<Int, Int> = mapOf(
        65 to R.string.program_cotton_resistant,
        2 to R.string.program_cotton_standard,
        3 to R.string.program_synthetics,
        5 to R.string.program_wool,
        4 to R.string.program_delicates,
        44 to R.string.program_rapid_14,
        43 to R.string.program_rapid_30,
        42 to R.string.program_rapid_44,
        40 to R.string.program_rapid_59,
        35 to R.string.program_rinse,
        129 to R.string.program_drain_spin,
        17 to R.string.program_steam,
    )

    /** Programmi avviabili da remoto, nell'ordine mostrato in UI. */
    val SELECTABLE: List<WashProgram> = listOf(
        WashProgram(pr = 1, prCode = 65, labelRes = R.string.program_cotton_resistant),
        WashProgram(pr = 2, prCode = 2, labelRes = R.string.program_cotton_standard),
        WashProgram(pr = 3, prCode = 3, labelRes = R.string.program_synthetics),
        WashProgram(pr = 4, prCode = 5, labelRes = R.string.program_wool),
        WashProgram(pr = 5, prCode = 4, labelRes = R.string.program_delicates),
        WashProgram(pr = 15, prCode = 44, labelRes = R.string.program_rapid_14),
        WashProgram(pr = 15, prCode = 43, labelRes = R.string.program_rapid_30),
        WashProgram(pr = 15, prCode = 42, labelRes = R.string.program_rapid_44),
        WashProgram(pr = 7, prCode = 35, labelRes = R.string.program_rinse),
        WashProgram(pr = 8, prCode = 129, labelRes = R.string.program_drain_spin),
    )

    @StringRes
    fun labelRes(prCode: Int): Int? = LABELS[prCode]
}

/** Temperature selezionabili (°C, 0 = freddo). */
object Temperatures {
    private const val COLD = 0

    val SELECTABLE: List<SelectableOption> = listOf(COLD, 20, 30, 40, 60, 90).map { value ->
        SelectableOption(
            value = value,
            label = if (value == COLD) {
                UiText.Resource(R.string.temperature_cold)
            } else {
                UiText.Resource(R.string.value_temperature, value)
            },
        )
    }
}

/** Velocità di centrifuga selezionabili (in centinaia di rpm, 0 = nessuna). */
object SpinSpeeds {
    private const val NONE = 0

    val SELECTABLE: List<SelectableOption> = listOf(NONE, 4, 6, 8, 10, 12).map { value ->
        SelectableOption(
            value = value,
            label = if (value == NONE) {
                UiText.Resource(R.string.spin_none)
            } else {
                UiText.Resource(R.string.value_rpm, value * CandyProtocol.SPIN_MULTIPLIER)
            },
        )
    }
}
