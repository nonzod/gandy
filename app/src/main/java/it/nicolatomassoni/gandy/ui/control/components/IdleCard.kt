package it.nicolatomassoni.gandy.ui.control.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.model.WashingMachineStatus
import it.nicolatomassoni.gandy.ui.components.InfoRow
import it.nicolatomassoni.gandy.ui.components.StatusBadge
import it.nicolatomassoni.gandy.ui.theme.phaseStyleOf

@Composable
internal fun IdleCard(status: WashingMachineStatus, loading: Boolean) {
    val colors = MaterialTheme.colorScheme
    val style = phaseStyleOf(status)
    val showIllustration = status.remoteReady || status.isDone || status.hasError

    if (!showIllustration) {
        SimpleIdleCard(status, loading, style)
        return
    }

    Surface(
        color = style.tint,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box {
            if (style.showCircles) {
                DecorativeCircles(style.ink)
            }
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatusBadge(status.machineModeLabel.asString().uppercase(), style.ink)
                        if (status.remoteReady && !status.isDone && !status.hasError) {
                            Text(
                                stringResource(R.string.control_remote_active),
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = style.ink,
                            )
                        }
                    }
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                }

                val illustrationHeight = when {
                    status.hasError -> 140
                    status.isDone -> 170
                    else -> 112
                }
                IllustrationArea(style, illustrationHeight)

                if (status.isDone) {
                    Text(
                        stringResource(R.string.control_done_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = style.contentColor,
                    )
                }

                if (status.hasError) {
                    Text(
                        stringResource(R.string.control_error_code, status.error),
                        color = style.ink,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                HorizontalDivider(color = style.ink.copy(alpha = 0.2f))

                InfoRow(
                    stringResource(R.string.label_program),
                    status.programLabel.asString(),
                    labelColor = style.contentColor.copy(alpha = 0.75f),
                    valueColor = style.contentColor,
                )
                InfoRow(
                    stringResource(R.string.label_temperature),
                    stringResource(R.string.value_temperature, status.temperature),
                    labelColor = style.contentColor.copy(alpha = 0.75f),
                    valueColor = style.contentColor,
                )
                InfoRow(
                    stringResource(R.string.label_spin),
                    stringResource(R.string.value_rpm, status.spinRpm),
                    labelColor = style.contentColor.copy(alpha = 0.75f),
                    valueColor = style.contentColor,
                )
            }
        }
    }
}

@Composable
private fun SimpleIdleCard(
    status: WashingMachineStatus,
    loading: Boolean,
    style: it.nicolatomassoni.gandy.ui.theme.PhaseStyle,
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        color = colors.surfaceVariant.copy(alpha = 0.5f),
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
                StatusBadge(status.machineModeLabel.asString().uppercase(), style.ink)
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }

            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.5f))

            InfoRow(stringResource(R.string.label_program), status.programLabel.asString())
            InfoRow(
                stringResource(R.string.label_temperature),
                stringResource(R.string.value_temperature, status.temperature),
            )
            InfoRow(
                stringResource(R.string.label_spin),
                stringResource(R.string.value_rpm, status.spinRpm),
            )
        }
    }
}
