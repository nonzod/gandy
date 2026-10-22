package it.tourtools.gandy.ui.control.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.tourtools.gandy.R
import it.tourtools.gandy.data.model.WashingMachineStatus
import it.tourtools.gandy.ui.components.StatusBadge

@Composable
internal fun RunningCard(status: WashingMachineStatus, loading: Boolean, cycleTotalTime: Int) {
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
                    stringResource(if (isPaused) R.string.control_badge_paused else R.string.control_badge_running),
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
                stringResource(R.string.control_running_details, status.temperature, status.spinRpm),
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

private const val RING_START_ANGLE = -90f

private const val FULL_CIRCLE = 360f
