package com.gcoded.meditrack.ui.screens.medication

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MedicationFormStateTest {

    private val today = LocalDate.of(2026, 10, 24)

    private fun validState() = MedicationFormState(
        name = "Aspirin",
        dosage = "100 mg",
        startDate = today
    )

    @Test
    fun filledFormWithDefaultsIsValid() {
        assertTrue(validState().isValid)
    }

    @Test
    fun blankNameIsInvalid() {
        assertFalse(validState().copy(name = "   ").isValid)
    }

    @Test
    fun blankDosageIsInvalid() {
        assertFalse(validState().copy(dosage = "").isValid)
    }

    @Test
    fun noTimesIsInvalid() {
        assertFalse(validState().copy(times = emptyList()).isValid)
    }

    @Test
    fun noDaysIsInvalid() {
        assertFalse(validState().copy(days = emptySet()).isValid)
    }

    @Test
    fun endDateBeforeStartDateIsInvalid() {
        val state = validState().copy(endDate = today.minusDays(1))
        assertFalse(state.isPeriodValid)
        assertFalse(state.isValid)
    }

    @Test
    fun endDateOnStartDateIsValid() {
        assertTrue(validState().copy(endDate = today).isValid)
    }

    @Test
    fun editingFlagFollowsMedicationId() {
        assertFalse(validState().isEditing)
        assertTrue(validState().copy(editingMedicationId = 7L).isEditing)
    }
}
