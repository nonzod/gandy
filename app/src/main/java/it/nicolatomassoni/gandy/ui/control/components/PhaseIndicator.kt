package it.nicolatomassoni.gandy.ui.control.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import it.nicolatomassoni.gandy.data.ProgramPhase

@Composable
internal fun PhaseIndicator(currentPhase: Int) {
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
