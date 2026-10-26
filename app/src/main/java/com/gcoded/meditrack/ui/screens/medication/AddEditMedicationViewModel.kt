package com.gcoded.meditrack.ui.screens.medication

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcoded.meditrack.domain.model.DoseSchedule
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationForm
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import com.gcoded.meditrack.domain.repository.MedicationRepository
import com.gcoded.meditrack.ui.navigation.MediTrackDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddEditMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editingMedicationId: Long? =
        savedStateHandle.get<Long>(MediTrackDestination.AddEditMedication.ARG_MEDICATION_ID)
            ?.takeIf { it != MediTrackDestination.AddEditMedication.NO_MEDICATION_ID }

    private val _formState = MutableStateFlow(MedicationFormState(startDate = LocalDate.now(clock)))
    val formState: StateFlow<MedicationFormState> = _formState.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private var isSaving = false

    init {
        editingMedicationId?.let { id ->
            viewModelScope.launch {
                val item = medicationRepository.getMedication(id) ?: return@launch
                val medication = item.medication
                _formState.value = MedicationFormState(
                    name = medication.name,
                    dosage = medication.dosage,
                    form = medication.form,
                    notes = medication.notes,
                    times = item.schedules.map { it.time }.distinct().sorted(),
                    days = item.schedules.firstOrNull()?.daysOfWeek ?: DayOfWeek.entries.toSet(),
                    startDate = medication.startDate,
                    endDate = medication.endDate,
                    editingMedicationId = medication.id,
                    isActive = medication.isActive
                )
            }
        }
    }

    fun onNameChange(value: String) = _formState.update { it.copy(name = value) }

    fun onDosageChange(value: String) = _formState.update { it.copy(dosage = value) }

    fun onFormSelected(form: MedicationForm) = _formState.update { it.copy(form = form) }

    fun onNotesChange(value: String) = _formState.update { it.copy(notes = value) }

    fun onTimeAdded(time: LocalTime) = _formState.update {
        it.copy(times = (it.times + time).distinct().sorted())
    }

    fun onTimeChanged(index: Int, time: LocalTime) = _formState.update {
        val updated = it.times.toMutableList()
        if (index in updated.indices) updated[index] = time
        it.copy(times = updated.distinct().sorted())
    }

    fun onTimeRemoved(index: Int) = _formState.update {
        it.copy(times = it.times.filterIndexed { i, _ -> i != index })
    }

    fun onDayToggled(day: DayOfWeek, checked: Boolean) = _formState.update {
        it.copy(days = if (checked) it.days + day else it.days - day)
    }

    fun onStartDateChanged(date: LocalDate) = _formState.update { it.copy(startDate = date) }

    fun onEndDateChanged(date: LocalDate?) = _formState.update { it.copy(endDate = date) }

    fun onSaveClick() {
        val state = _formState.value
        if (!state.isValid || isSaving) return
        isSaving = true

        viewModelScope.launch {
            medicationRepository.saveMedication(
                MedicationWithSchedules(
                    medication = Medication(
                        id = state.editingMedicationId ?: 0,
                        name = state.name.trim(),
                        dosage = state.dosage.trim(),
                        form = state.form,
                        notes = state.notes.trim(),
                        isActive = state.isActive,
                        startDate = state.startDate,
                        endDate = state.endDate
                    ),
                    schedules = state.times.map { DoseSchedule(time = it, daysOfWeek = state.days) }
                )
            )
            _isSaved.value = true
        }
    }
}
