package it.tourtools.gandy.ui.control.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import it.tourtools.gandy.R
import it.tourtools.gandy.data.SelectableOption
import it.tourtools.gandy.data.SpinSpeeds
import it.tourtools.gandy.data.Temperatures
import it.tourtools.gandy.data.WashPrograms
import it.tourtools.gandy.ui.components.SelectionDropdown
import it.tourtools.gandy.ui.control.ControlActions
import it.tourtools.gandy.ui.control.ControlUiState

@Composable
internal fun ProgramControls(state: ControlUiState, actions: ControlActions) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.control_program_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            SelectionDropdown(
                label = stringResource(R.string.label_program),
                selectedLabel = WashPrograms.SELECTABLE.find { it.prCode == state.selectedPrCode }
                    ?.let { stringResource(it.labelRes) }
                    ?: stringResource(R.string.control_select_placeholder),
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
        Text(stringResource(R.string.control_start_program), style = MaterialTheme.typography.titleMedium)
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
