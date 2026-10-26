package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseSchedule
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationForm
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FindNextReminderUseCaseTest {

    private val useCase = FindNextReminderUseCase(BuildDaySlotsUseCase())

    // A Friday
    private val friday = LocalDate.of(2026, 10, 23)

    private fun item(
        id: Long = 1,
        name: String = "Aspirin",
        isActive: Boolean = true,
        startDate: LocalDate = friday.minusDays(30),
        vararg times: LocalTime,
        days: Set<DayOfWeek> = DayOfWeek.entries.toSet()
    ) = MedicationWithSchedules(
        medication = Medication(
            id = id,
            name = name,
            dosage = "100 mg",
            form = MedicationForm.PILL,
            notes = "",
            isActive = isActive,
            startDate = startDate
        ),
        schedules = times.map { DoseSchedule(time = it, daysOfWeek = days) }
    )

    private fun at(hour: Int, minute: Int = 0): LocalDateTime = friday.atTime(hour, minute)

    private val eight = LocalTime.of(8, 0)
    private val twenty = LocalTime.of(20, 0)

    @Test
    fun noMedicationsGivesNoTrigger() {
        assertNull(useCase(emptyList(), emptyList(), at(12)))
    }

    @Test
    fun picksTheEarliestUpcomingSlotToday() {
        val trigger = useCase(listOf(item(times = arrayOf(eight, twenty))), emptyList(), at(12))
        assertEquals(friday.atTime(twenty), trigger?.triggerAt)
    }

    @Test
    fun aSlotExactlyAtNowIsDueNotUpcoming() {
        val trigger = useCase(listOf(item(times = arrayOf(eight, twenty))), emptyList(), at(8, 0))
        assertEquals(friday.atTime(twenty), trigger?.triggerAt)
    }

    @Test
    fun anAlreadyLoggedSlotIsSkipped() {
        val log = DoseLog(
            medicationId = 1,
            date = friday,
            time = eight,
            status = DoseStatus.TAKEN,
            loggedAt = Instant.EPOCH
        )
        val trigger = useCase(listOf(item(times = arrayOf(eight, twenty))), listOf(log), at(7))
        assertEquals(friday.atTime(twenty), trigger?.triggerAt)
    }

    @Test
    fun rollsOverToTomorrowWhenTodayIsDone() {
        val trigger = useCase(listOf(item(times = arrayOf(eight, twenty))), emptyList(), at(21))
        assertEquals(friday.plusDays(1).atTime(eight), trigger?.triggerAt)
    }

    @Test
    fun skipsDaysWhereTheMedicationIsNotScheduled() {
        val mondayOnly = item(times = arrayOf(eight), days = setOf(DayOfWeek.MONDAY))
        val trigger = useCase(listOf(mondayOnly), emptyList(), at(7))
        // Friday 23 Oct -> next Monday is 26 Oct
        assertEquals(LocalDate.of(2026, 10, 26).atTime(eight), trigger?.triggerAt)
    }

    @Test
    fun groupsEverySlotDueAtTheSameMoment() {
        val a = item(id = 1, name = "Aspirin", times = arrayOf(eight))
        val b = item(id = 2, name = "Zinc", times = arrayOf(eight))
        val trigger = useCase(listOf(a, b), emptyList(), at(7))
        assertNotNull(trigger)
        assertEquals(2, trigger!!.slots.size)
        assertEquals(setOf("Aspirin", "Zinc"), trigger.slots.map { it.medication.name }.toSet())
    }

    @Test
    fun inactiveMedicationsNeverTrigger() {
        val inactive = item(isActive = false, times = arrayOf(eight))
        assertNull(useCase(listOf(inactive), emptyList(), at(7)))
    }

    @Test
    fun nothingBeyondTheLookaheadWindow() {
        val startsLater = item(startDate = friday.plusDays(20), times = arrayOf(eight))
        assertNull(useCase(listOf(startsLater), emptyList(), at(7)))
    }
}
