package it.tourtools.gandy.ui.provision.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.tourtools.gandy.R
import it.tourtools.gandy.ui.components.NumberedStep

@Composable
internal fun SaveDeviceStep(
    generatedKey: String,
    deviceName: String,
    deviceIp: String,
    onNameChange: (String) -> Unit,
    onIpChange: (String) -> Unit,
    onComplete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.provision_sent_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.provision_sent_description),
                style = MaterialTheme.typography.bodyMedium,
            )
            NumberedStep(1, stringResource(R.string.provision_final_1))
            NumberedStep(2, stringResource(R.string.provision_final_2))
            NumberedStep(3, stringResource(R.string.provision_final_3))
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.provision_key),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    generatedKey,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            OutlinedTextField(
                value = deviceName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.provision_device_name)) },
                placeholder = { Text(stringResource(R.string.placeholder_device_name)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = deviceIp,
                onValueChange = onIpChange,
                label = { Text(stringResource(R.string.label_ip_address)) },
                placeholder = { Text(stringResource(R.string.placeholder_ip_address)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    Button(
        onClick = onComplete,
        enabled = deviceName.isNotBlank() && deviceIp.isNotBlank(),
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(stringResource(R.string.provision_save), style = MaterialTheme.typography.titleMedium)
    }
}
