package it.nicolatomassoni.gandy.ui.devices.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.model.Appliance
import it.nicolatomassoni.gandy.ui.theme.Ph3ContainerDark
import it.nicolatomassoni.gandy.ui.theme.Ph3ContainerLight
import it.nicolatomassoni.gandy.ui.theme.Ph3Dark
import it.nicolatomassoni.gandy.ui.theme.Ph3Light
import it.nicolatomassoni.gandy.ui.theme.Ph4ContainerDark
import it.nicolatomassoni.gandy.ui.theme.Ph4ContainerLight
import it.nicolatomassoni.gandy.ui.theme.Ph4Dark
import it.nicolatomassoni.gandy.ui.theme.Ph4Light

private data class DeviceColorPair(val container: Color, val icon: Color)

@Composable
private fun deviceColorPairs(): List<DeviceColorPair> {
    val colors = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    return listOf(
        DeviceColorPair(colors.primaryContainer, colors.primary),
        DeviceColorPair(
            if (dark) Ph3ContainerDark else Ph3ContainerLight,
            if (dark) Ph3Dark else Ph3Light,
        ),
        DeviceColorPair(
            if (dark) Ph4ContainerDark else Ph4ContainerLight,
            if (dark) Ph4Dark else Ph4Light,
        ),
    )
}

@Composable
internal fun DeviceCard(
    appliance: Appliance,
    colorIndex: Int = 0,
    onSelect: () -> Unit,
    onShare: () -> Unit,
    onRemove: () -> Unit,
) {
    val colorPairs = deviceColorPairs()
    val pair = colorPairs[colorIndex % colorPairs.size]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(pair.container),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.LocalLaundryService,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = pair.icon,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    appliance.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    appliance.ip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onShare) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = stringResource(R.string.action_share),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_remove),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp),
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}
