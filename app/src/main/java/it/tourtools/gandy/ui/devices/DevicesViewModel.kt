package it.tourtools.gandy.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import it.tourtools.gandy.data.CandyRepository
import it.tourtools.gandy.data.model.Appliance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val repo: CandyRepository,
) : ViewModel() {

    val appliances: StateFlow<List<Appliance>> = repo.appliances.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList(),
    )

    fun addAppliance(appliance: Appliance) {
        viewModelScope.launch { repo.addAppliance(appliance) }
    }

    fun removeAppliance(id: String) {
        viewModelScope.launch { repo.removeAppliance(id) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
