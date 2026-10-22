package it.tourtools.gandy.ui.provision.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.tourtools.gandy.R
import it.tourtools.gandy.ui.components.NumberedStep

@Composable
internal fun InstructionsStep(onNext: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.provision_step_preparation),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            NumberedStep(1, stringResource(R.string.provision_instruction_1))
            NumberedStep(2, stringResource(R.string.provision_instruction_2))
            NumberedStep(3, stringResource(R.string.provision_instruction_3))
            NumberedStep(4, stringResource(R.string.provision_instruction_4))
        }
    }

    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Text(stringResource(R.string.provision_connected), style = MaterialTheme.typography.titleMedium)
    }
}
