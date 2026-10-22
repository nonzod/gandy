package it.tourtools.gandy.ui.control.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.tourtools.gandy.R
import it.tourtools.gandy.data.model.WashingMachineStatus
import it.tourtools.gandy.ui.components.InfoRow
import it.tourtools.gandy.ui.components.StatusBadge

@Composable
internal fun IdleCard(status: WashingMachineStatus, loading: Boolean) {
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
                    stringResource(R.string.control_error_code, status.error),
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
