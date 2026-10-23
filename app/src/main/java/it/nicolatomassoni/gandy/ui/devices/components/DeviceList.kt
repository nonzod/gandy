package it.nicolatomassoni.gandy.ui.devices.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import it.nicolatomassoni.gandy.R
import it.nicolatomassoni.gandy.data.model.Appliance

@Composable
internal fun DeviceList(
    appliances: List<Appliance>,
    onSelect: (Appliance) -> Unit,
    onShare: (Appliance) -> Unit,
    onRemove: (String) -> Unit,
    onProvision: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        itemsIndexed(appliances, key = { _, a -> a.id }) { index, appliance ->
            DeviceCard(
                appliance = appliance,
                colorIndex = index,
                onSelect = { onSelect(appliance) },
                onShare = { onShare(appliance) },
                onRemove = { onRemove(appliance.id) },
            )
        }
        item {
            OutlinedButton(
                onClick = onProvision,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Outlined.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.devices_provision))
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}
