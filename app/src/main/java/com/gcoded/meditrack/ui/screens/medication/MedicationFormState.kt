package com.gcoded.meditrack.ui.screens.medication

import com.gcoded.meditrack.domain.model.MedicationForm
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

data class MedicationFormState(
    val name: String = "",
    val dosage: String = "",
    val form: MedicationForm = MedicationForm.PILL,
    val notes: String = "",
    val times: List<LocalTime> = listOf(LocalTime.of(8, 0)),
    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val editingMedicationId: Long? = null,
    val isActive: Boolean = true
) {
    val isEditing: Boolean get() = editingMedicationId != null
    val hasTimes: Boolean get() = times.isNotEmpty()
    val hasDays: Boolean get() = days.isNotEmpty()
    val isPeriodValid: Boolean get() = endDate == null || !endDate.isBefore(startDate)
    val isValid: Boolean
        get() = name.isNotBlank() && dosage.isNotBlank() && hasTimes && hasDays && isPeriodValid
}
