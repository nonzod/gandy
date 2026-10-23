package it.nicolatomassoni.gandy.ui.control.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.res.imageResource
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.model.WashingMachineStatus
import it.nicolatomassoni.gandy.ui.components.InfoRow
import it.nicolatomassoni.gandy.ui.components.StatusBadge
import it.nicolatomassoni.gandy.ui.theme.PhaseStyle
import it.nicolatomassoni.gandy.ui.theme.phaseStyleOf

@Composable
internal fun RunningCard(status: WashingMachineStatus, loading: Boolean, cycleTotalTime: Int) {
    val style = phaseStyleOf(status)

    Surface(
        color = style.tint,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box {
            DecorativeCircles(style.ink)
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(
                        stringResource(
                            if (status.isPaused) R.string.control_badge_paused
                            else R.string.control_badge_running,
                        ),
                        style.ink,
                    )
                    PhaseBadge(status.programPhaseLabel.asString(), style.ink)
                }
                Spacer(Modifier.height(16.dp))
                IllustrationArea(style)
                Spacer(Modifier.height(16.dp))
                TimerRing(status, cycleTotalTime, style)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = style.ink.copy(alpha = 0.2f))
                Spacer(Modifier.height(16.dp))
                InfoRow(
                    stringResource(R.string.label_program),
                    status.programLabel.asString(),
                    labelColor = style.contentColor.copy(alpha = 0.75f),
                    valueColor = style.contentColor,
                )
                Spacer(Modifier.height(12.dp))
                InfoRow(
                    stringResource(R.string.label_temperature),
                    stringResource(R.string.value_temperature, status.temperature),
                    labelColor = style.contentColor.copy(alpha = 0.75f),
                    valueColor = style.contentColor,
                )
                Spacer(Modifier.height(12.dp))
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
internal fun DecorativeCircles(ink: Color) {
    Box(modifier = Modifier.fillMaxWidth().height(0.dp)) {
        Canvas(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .offset(x = 18.dp, y = (-18).dp),
        ) {
            drawCircle(
                color = ink,
                radius = size.minDimension / 2,
                style = Stroke(width = 2.dp.toPx()),
                alpha = 0.2f,
            )
        }
        Box(
            modifier = Modifier
                .size(12.dp)
                .offset(x = (-58).dp, y = 40.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(ink.copy(alpha = 0.25f)),
        )
    }
}

@Composable
internal fun IllustrationArea(style: PhaseStyle, height: Int = 170) {
    val bg = lerp(MaterialTheme.colorScheme.surface, style.ink, 0.12f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(style.illustration),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
        )
    }
}

@Composable
private fun PhaseBadge(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun TimerRing(status: WashingMachineStatus, cycleTotalTime: Int, style: PhaseStyle) {
    val trackColor = style.ink.copy(alpha = 0.15f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val strokeWidth = 8.dp.toPx()
                val arcSize = size.width - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = stroke,
                )

                val sweep = if (cycleTotalTime > 0 && status.remainingTime >= 0) {
                    val elapsed = (cycleTotalTime - status.remainingTime).coerceAtLeast(0)
                    (elapsed.toFloat() / cycleTotalTime * 360f).coerceIn(0f, 360f)
                } else {
                    0f
                }

                drawArc(
                    color = style.ink,
                    startAngle = -90f,
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
                    color = style.contentColor,
                )
                Text(
                    status.programPhaseLabel.asString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = style.contentColor.copy(alpha = 0.7f),
                )
            }
        }
    }
}
