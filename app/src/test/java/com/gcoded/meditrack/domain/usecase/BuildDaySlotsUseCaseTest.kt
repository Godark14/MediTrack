package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.DaySummary
import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseSchedule
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationForm
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BuildDaySlotsUseCaseTest {

    private val useCase = BuildDaySlotsUseCase()

    // Friday
    private val date = LocalDate.of(2026, 10, 23)

    private fun medication(
        id: Long = 1,
        name: String = "Aspirin",
        isActive: Boolean = true,
        startDate: LocalDate = date.minusDays(10),
        endDate: LocalDate? = null
    ) = Medication(
        id = id,
        name = name,
        dosage = "100 mg",
        form = MedicationForm.PILL,
        notes = "",
        isActive = isActive,
        startDate = startDate,
        endDate = endDate
    )

    private fun item(
        medication: Medication,
        vararg times: LocalTime,
        days: Set<DayOfWeek> = DayOfWeek.entries.toSet()
    ) = MedicationWithSchedules(
        medication = medication,
        schedules = times.map { DoseSchedule(time = it, daysOfWeek = days) }
    )

    private fun log(medicationId: Long, time: LocalTime, status: DoseStatus) = DoseLog(
        medicationId = medicationId,
        date = date,
        time = time,
        status = status,
        loggedAt = Instant.EPOCH
    )

    private val eight = LocalTime.of(8, 0)

    @Test
    fun slotBeforeItsTimeIsUpcoming() {
        val slots = useCase(date, listOf(item(medication(), eight)), emptyList(), date.atTime(7, 59))
        assertEquals(DoseSlotStatus.UPCOMING, slots.single().status)
    }

    @Test
    fun slotAtItsTimeIsDue() {
        val slots = useCase(date, listOf(item(medication(), eight)), emptyList(), date.atTime(8, 0))
        assertEquals(DoseSlotStatus.DUE, slots.single().status)
    }

    @Test
    fun slotJustInsideWindowIsStillDue() {
        val slots = useCase(date, listOf(item(medication(), eight)), emptyList(), date.atTime(9, 59))
        assertEquals(DoseSlotStatus.DUE, slots.single().status)
    }

    @Test
    fun slotAfterWindowIsMissed() {
        val slots = useCase(date, listOf(item(medication(), eight)), emptyList(), date.atTime(10, 0))
        assertEquals(DoseSlotStatus.MISSED, slots.single().status)
    }

    @Test
    fun takenLogOverridesTimeBasedStatus() {
        val slots = useCase(
            date,
            listOf(item(medication(), eight)),
            listOf(log(1, eight, DoseStatus.TAKEN)),
            date.atTime(23, 0)
        )
        assertEquals(DoseSlotStatus.TAKEN, slots.single().status)
    }

    @Test
    fun skippedLogGivesSkippedStatus() {
        val slots = useCase(
            date,
            listOf(item(medication(), eight)),
            listOf(log(1, eight, DoseStatus.SKIPPED)),
            date.atTime(8, 30)
        )
        assertEquals(DoseSlotStatus.SKIPPED, slots.single().status)
    }

    @Test
    fun inactiveMedicationIsExcludedUnlessRequested() {
        val inactive = item(medication(isActive = false), eight)
        val now = date.atTime(7, 0)
        assertTrue(useCase(date, listOf(inactive), emptyList(), now).isEmpty())
        assertEquals(1, useCase(date, listOf(inactive), emptyList(), now, includeInactive = true).size)
    }

    @Test
    fun medicationNotScheduledOnThisWeekdayIsExcluded() {
        val mondayOnly = item(medication(), eight, days = setOf(DayOfWeek.MONDAY))
        assertTrue(useCase(date, listOf(mondayOnly), emptyList(), date.atTime(7, 0)).isEmpty())
    }

    @Test
    fun medicationOutsideItsDateRangeIsExcluded() {
        val notStarted = item(medication(startDate = date.plusDays(1)), eight)
        val ended = item(medication(endDate = date.minusDays(1)), eight)
        val now = date.atTime(7, 0)
        assertTrue(useCase(date, listOf(notStarted), emptyList(), now).isEmpty())
        assertTrue(useCase(date, listOf(ended), emptyList(), now).isEmpty())
    }

    @Test
    fun slotsAreSortedByTimeThenName() {
        val zinc = item(medication(id = 1, name = "Zinc"), eight, LocalTime.of(20, 0))
        val aspirin = item(medication(id = 2, name = "Aspirin"), eight)
        val slots = useCase(date, listOf(zinc, aspirin), emptyList(), date.atTime(7, 0))
        assertEquals(
            listOf("Aspirin" to eight, "Zinc" to eight, "Zinc" to LocalTime.of(20, 0)),
            slots.map { it.medication.name to it.time }
        )
    }

    @Test
    fun unloggedSlotOnPastDayIsMissedAndOnFutureDayIsUpcoming() {
        val now = date.atTime(12, 0)
        val schedule = listOf(item(medication(), eight))
        assertEquals(
            DoseSlotStatus.MISSED,
            useCase(date.minusDays(1), schedule, emptyList(), now).single().status
        )
        assertEquals(
            DoseSlotStatus.UPCOMING,
            useCase(date.plusDays(1), schedule, emptyList(), now).single().status
        )
    }

    @Test
    fun summaryProgressIsTakenOverTotal() {
        val med = medication()
        val slots = listOf(
            DoseSlot(med, date, LocalTime.of(8, 0), DoseSlotStatus.TAKEN),
            DoseSlot(med, date, LocalTime.of(12, 0), DoseSlotStatus.SKIPPED),
            DoseSlot(med, date, LocalTime.of(16, 0), DoseSlotStatus.DUE),
            DoseSlot(med, date, LocalTime.of(20, 0), DoseSlotStatus.UPCOMING)
        )
        val summary = DaySummary(date, slots)
        assertEquals(4, summary.totalCount)
        assertEquals(1, summary.takenCount)
        assertEquals(0.25f, summary.progress, 0.0001f)
    }

    @Test
    fun emptyDayHasZeroProgress() {
        assertEquals(0f, DaySummary(date, emptyList()).progress, 0.0001f)
    }
}
