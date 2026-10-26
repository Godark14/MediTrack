package com.gcoded.meditrack.ui.screens.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    val uiState: StateFlow<MedicationsUiState> = medicationRepository
        .observeMedications()
        .map { it.toMedicationsUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MedicationsUiState.Loading
        )

    private val _events = Channel<MedicationsEvent>(Channel.BUFFERED)
    val events: Flow<MedicationsEvent> = _events.receiveAsFlow()

    fun onSetActive(medication: Medication, isActive: Boolean) {
        viewModelScope.launch {
            medicationRepository.setActive(medication.id, isActive)
            _events.send(MedicationsEvent.ActiveChanged(medication, isActive))
        }
    }

    fun onUndoActiveChange(medication: Medication, previousIsActive: Boolean) {
        viewModelScope.launch {
            medicationRepository.setActive(medication.id, previousIsActive)
        }
    }

    fun onDelete(medication: Medication) {
        viewModelScope.launch {
            medicationRepository.deleteMedication(medication.id)
            _events.send(MedicationsEvent.Deleted(medication))
        }
    }
}
