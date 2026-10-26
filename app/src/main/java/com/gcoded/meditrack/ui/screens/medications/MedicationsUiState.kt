package com.gcoded.meditrack.ui.screens.medications

import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationWithSchedules

sealed interface MedicationsUiState {
    data object Loading : MedicationsUiState
    data object Empty : MedicationsUiState
    data class Content(
        val active: List<MedicationWithSchedules>,
        val archived: List<MedicationWithSchedules>
    ) : MedicationsUiState
}

sealed interface MedicationsEvent {
    /** [isActive] is the NEW state; undoing restores the opposite. */
    data class ActiveChanged(val medication: Medication, val isActive: Boolean) : MedicationsEvent
    data class Deleted(val medication: Medication) : MedicationsEvent
}

internal fun List<MedicationWithSchedules>.toMedicationsUiState(): MedicationsUiState =
    if (isEmpty()) {
        MedicationsUiState.Empty
    } else {
        MedicationsUiState.Content(
            active = filter { it.medication.isActive },
            archived = filter { !it.medication.isActive }
        )
    }
