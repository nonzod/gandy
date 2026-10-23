package it.nicolatomassoni.gandy.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.MachineMode
import it.nicolatomassoni.gandy.data.ProgramPhase
import it.nicolatomassoni.gandy.data.model.WashingMachineStatus

data class PhaseStyle(
    val ink: Color,
    val tint: Color,
    val contentColor: Color,
    @DrawableRes val illustration: Int,
    val showCircles: Boolean = true,
)

@Composable
fun cardColor(): Color = if (isSystemInDarkTheme()) CardDark else CardLight

@Composable
fun phaseStyleOf(status: WashingMachineStatus): PhaseStyle {
    val colors = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()

    return when {
        status.hasError -> PhaseStyle(
            ink = colors.error,
            tint = colors.errorContainer,
            contentColor = colors.onErrorContainer,
            illustration = R.drawable.ill_phase_error,
            showCircles = false,
        )
        status.isDone -> PhaseStyle(
            ink = colors.tertiary,
            tint = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer,
            illustration = R.drawable.ill_phase_done,
        )
        status.isPaused -> PhaseStyle(
            ink = colors.secondary,
            tint = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
            illustration = R.drawable.ill_phase_paused,
        )
        status.isRunning -> runningPhaseStyle(status.programPhase, colors, dark)
        else -> PhaseStyle(
            ink = colors.primary,
            tint = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
            illustration = R.drawable.ill_phase_ready,
        )
    }
}

@Composable
private fun runningPhaseStyle(
    phase: Int,
    colors: androidx.compose.material3.ColorScheme,
    dark: Boolean,
): PhaseStyle = when (phase) {
    ProgramPhase.PREWASH -> PhaseStyle(
        ink = if (dark) Ph1Dark else Ph1Light,
        tint = if (dark) Ph1ContainerDark else Ph1ContainerLight,
        contentColor = colors.onSurface,
        illustration = R.drawable.ill_phase_prewash,
    )
    ProgramPhase.RINSE, ProgramPhase.LAST_RINSE -> PhaseStyle(
        ink = if (dark) Ph3Dark else Ph3Light,
        tint = if (dark) Ph3ContainerDark else Ph3ContainerLight,
        contentColor = colors.onSurface,
        illustration = R.drawable.ill_phase_rinse,
    )
    in ProgramPhase.SPIN -> PhaseStyle(
        ink = if (dark) Ph4Dark else Ph4Light,
        tint = if (dark) Ph4ContainerDark else Ph4ContainerLight,
        contentColor = colors.onSurface,
        illustration = R.drawable.ill_phase_spin,
    )
    else -> PhaseStyle(
        ink = colors.primary,
        tint = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        illustration = R.drawable.ill_phase_wash,
    )
}
