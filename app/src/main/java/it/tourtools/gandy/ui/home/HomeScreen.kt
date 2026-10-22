package it.tourtools.gandy.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import it.tourtools.gandy.R
import it.tourtools.gandy.data.ProgramPhase
import it.tourtools.gandy.data.SelectableOption
import it.tourtools.gandy.data.SpinSpeeds
import it.tourtools.gandy.data.Temperatures
import it.tourtools.gandy.data.WashPrograms
import it.tourtools.gandy.data.model.WashingMachineStatus
import it.tourtools.gandy.ui.components.ErrorCard
import it.tourtools.gandy.ui.components.GandyTopBar
import it.tourtools.gandy.ui.components.InfoRow
import it.tourtools.gandy.ui.components.SelectionDropdown
import it.tourtools.gandy.ui.components.StatusBadge

/** Azioni dell'utente sulla schermata di controllo. */
private class HomeActions(
    val onStart: () -> Unit,
    val onStop: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onSelectProgram: (pr: Int, prCode: Int) -> Unit,
    val onSelectTemp: (Int) -> Unit,
    val onSelectSpin: (Int) -> Unit,
    val onDismissError: () -> Unit,
)

@Composable
fun HomeScreen(
    ip: String,
    deviceName: String,
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(ip) { viewModel.connect(ip) }

    val actions = remember(viewModel) {
        HomeActions(
            onStart = viewModel::startProgram,
            onStop = viewModel::stopProgram,
            onPause = viewModel::pause,
            onResume = viewModel::resume,
            onSelectProgram = viewModel::setProgram,
            onSelectTemp = viewModel::setTemp,
            onSelectSpin = viewModel::setSpin,
            onDismissError = viewModel::clearError,
        )
    }

    Scaffold(
        topBar = {
            GandyTopBar(
                title = { Text(deviceName, fontWeight = FontWeight.SemiBold) },
                onBack = onBack,
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.action_refresh))
                    }
                },
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
            StatusContent(state, actions)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusContent(state: HomeUiState, actions: HomeActions) {
    val status = state.status

    if (state.loading && status == null) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 120.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (status != null) {
        if (!status.remoteReady) {
            RemoteWarningCard()
        }

        if (status.isRunning || status.isPaused) {
            RunningCard(status, state.loading, state.cycleTotalTime)
            PhaseIndicator(status.programPhase)
            ActionButtons(
                isPaused = status.isPaused,
                onPause = actions.onPause,
                onResume = actions.onResume,
                onStop = actions.onStop,
            )
        } else {
            IdleCard(status, state.loading)
            if (status.isDone) {
                DoneActions(onReset = actions.onStop)
            } else if (status.remoteReady) {
                ProgramControls(state, actions)
            }
        }
    }

    state.error?.let { error ->
        ErrorCard(error.asString(), onDismiss = actions.onDismissError)
    }
}

// --- Running state ---

@Composable
private fun RunningCard(status: WashingMachineStatus, loading: Boolean, cycleTotalTime: Int) {
    val isPaused = status.isPaused
    val colors = MaterialTheme.colorScheme

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPaused) colors.secondaryContainer else colors.primaryContainer,
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusBadge(
                    stringResource(if (isPaused) R.string.home_badge_paused else R.string.home_badge_running),
                    if (isPaused) colors.secondary else colors.primary,
                )
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            TimerRing(status, cycleTotalTime)

            Spacer(Modifier.height(16.dp))

            val contentColor = if (isPaused) colors.onSecondaryContainer else colors.onPrimaryContainer

            Text(
                status.programLabel.asString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.home_running_details, status.temperature, status.spinRpm),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun TimerRing(status: WashingMachineStatus, cycleTotalTime: Int) {
    val isPaused = status.isPaused
    val colors = MaterialTheme.colorScheme
    val ringColor = if (isPaused) colors.secondary else colors.primary
    val trackColor = colors.outlineVariant.copy(alpha = 0.4f)
    val textColor = if (isPaused) colors.onSecondaryContainer else colors.onPrimaryContainer

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = 8.dp.toPx()
            val arcSize = size.width - strokeWidth
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

            drawArc(
                color = trackColor,
                startAngle = RING_START_ANGLE,
                sweepAngle = FULL_CIRCLE,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = stroke,
            )

            val sweep = if (cycleTotalTime > 0 && status.remainingTime >= 0) {
                val elapsed = (cycleTotalTime - status.remainingTime).coerceAtLeast(0)
                (elapsed.toFloat() / cycleTotalTime * FULL_CIRCLE).coerceIn(0f, FULL_CIRCLE)
            } else {
                0f
            }

            drawArc(
                color = ringColor,
                startAngle = RING_START_ANGLE,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = stroke,
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                status.remainingFormatted.asString(),
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 36.sp),
                fontWeight = FontWeight.Light,
                color = textColor,
            )
            Text(
                status.programPhaseLabel.asString(),
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun PhaseIndicator(currentPhase: Int) {
    val colors = MaterialTheme.colorScheme

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgramPhase.CYCLE_PHASES.forEachIndexed { index, (phase, labelRes) ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                        thickness = 2.dp,
                        color = if (currentPhase >= phase) colors.primary else colors.outlineVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // L'ultimo risciacquo è mostrato come fase di risciacquo.
                    val isCurrent = currentPhase == phase ||
                        (currentPhase == ProgramPhase.LAST_RINSE && phase == ProgramPhase.RINSE)
                    val isActive = currentPhase >= phase

                    val dotColor by animateColorAsState(
                        when {
                            isCurrent -> colors.primary
                            isActive -> colors.primary.copy(alpha = 0.5f)
                            else -> colors.outlineVariant
                        },
                        label = "phase",
                    )

                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(dotColor),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(labelRes),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) colors.primary else colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isPaused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isPaused) {
            Button(
                onClick = onResume,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.home_resume), style = MaterialTheme.typography.labelLarge)
            }
        } else {
            OutlinedButton(
                onClick = onPause,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(stringResource(R.string.home_pause), style = MaterialTheme.typography.labelLarge)
            }
        }

        FilledTonalButton(
            onClick = onStop,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
        ) {
            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.home_stop), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun RemoteWarningCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                stringResource(R.string.home_remote_warning),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

// --- Idle state ---

@Composable
private fun IdleCard(status: WashingMachineStatus, loading: Boolean) {
    val colors = MaterialTheme.colorScheme
    val containerColor = when {
        status.hasError -> colors.errorContainer
        status.isDone -> colors.tertiaryContainer
        else -> colors.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val badgeColor = when {
                    status.hasError -> colors.error
                    status.isDone -> colors.tertiary
                    else -> colors.primary
                }
                StatusBadge(status.machineModeLabel.asString().uppercase(), badgeColor)
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }

            if (status.hasError) {
                Text(
                    stringResource(R.string.home_error_code, status.error),
                    color = colors.error,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))

            InfoRow(stringResource(R.string.label_program), status.programLabel.asString())
            InfoRow(stringResource(R.string.label_temperature), stringResource(R.string.value_temperature, status.temperature))
            InfoRow(stringResource(R.string.label_spin), stringResource(R.string.value_rpm, status.spinRpm))
        }
    }
}

@Composable
private fun ProgramControls(state: HomeUiState, actions: HomeActions) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.home_program_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            SelectionDropdown(
                label = stringResource(R.string.label_program),
                selectedLabel = WashPrograms.SELECTABLE.find { it.prCode == state.selectedPrCode }
                    ?.let { stringResource(it.labelRes) }
                    ?: stringResource(R.string.home_select_placeholder),
                options = WashPrograms.SELECTABLE,
                optionLabel = { stringResource(it.labelRes) },
                onSelect = { actions.onSelectProgram(it.pr, it.prCode) },
            )
            OptionDropdown(
                label = stringResource(R.string.label_temperature),
                options = Temperatures.SELECTABLE,
                selected = state.selectedTemp,
                onSelect = actions.onSelectTemp,
            )
            OptionDropdown(
                label = stringResource(R.string.label_spin),
                options = SpinSpeeds.SELECTABLE,
                selected = state.selectedSpin,
                onSelect = actions.onSelectSpin,
            )
        }
    }

    Button(
        onClick = actions.onStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(stringResource(R.string.home_start_program), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun OptionDropdown(
    label: String,
    options: List<SelectableOption>,
    selected: Int,
    onSelect: (Int) -> Unit,
) {
    SelectionDropdown(
        label = label,
        selectedLabel = options.find { it.value == selected }?.label?.asString() ?: "$selected",
        options = options,
        optionLabel = { it.label.asString() },
        onSelect = { onSelect(it.value) },
    )
}

@Composable
private fun DoneActions(onReset: () -> Unit) {
    FilledTonalButton(
        onClick = onReset,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(stringResource(R.string.home_reset), style = MaterialTheme.typography.titleMedium)
    }
}

private const val RING_START_ANGLE = -90f
private const val FULL_CIRCLE = 360f
