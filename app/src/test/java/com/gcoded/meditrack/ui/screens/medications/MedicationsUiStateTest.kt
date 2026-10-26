package com.gcoded.meditrack.ui.screens.medications

import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationForm
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MedicationsUiStateTest {

    private val date = LocalDate.of(2026, 10, 24)

    private fun item(id: Long, name: String, isActive: Boolean) = MedicationWithSchedules(
        medication = Medication(
            id = id,
            name = name,
            dosage = "1",
            form = MedicationForm.PILL,
            notes = "",
            isActive = isActive,
            startDate = date
        ),
        schedules = emptyList()
    )

    @Test
    fun emptyListGivesEmptyState() {
        assertEquals(MedicationsUiState.Empty, emptyList<MedicationWithSchedules>().toMedicationsUiState())
    }

    @Test
    fun medicationsAreSplitIntoActiveAndArchivedKeepingOrder() {
        val state = listOf(
            item(1, "Aspirin", true),
            item(2, "Zinc", false),
            item(3, "Magnesium", true)
        ).toMedicationsUiState() as MedicationsUiState.Content

        assertEquals(listOf("Aspirin", "Magnesium"), state.active.map { it.medication.name })
        assertEquals(listOf("Zinc"), state.archived.map { it.medication.name })
    }

    @Test
    fun onlyArchivedStillGivesContentWithEmptyActive() {
        val state = listOf(item(1, "Zinc", false)).toMedicationsUiState() as MedicationsUiState.Content

        assertTrue(state.active.isEmpty())
        assertEquals(1, state.archived.size)
    }
}
