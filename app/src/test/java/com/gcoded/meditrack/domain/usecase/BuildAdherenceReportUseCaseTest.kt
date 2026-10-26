package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.DayAdherenceStatus
import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseSchedule
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationForm
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class BuildAdherenceReportUseCaseTest {

    private val useCase = BuildAdherenceReportUseCase(BuildDaySlotsUseCase())

    // A Sunday
    private val today = LocalDate.of(2026, 10, 25)
    private val eight = LocalTime.of(8, 0)
    private val twenty = LocalTime.of(20, 0)

    private val morning = today.atTime(7, 0)
    private val afternoon = today.atTime(12, 0)
    private val lateEvening = today.atTime(23, 0)

    private fun item(
        id: Long = 1,
        name: String = "Aspirin",
        isActive: Boolean = true,
        vararg times: LocalTime = arrayOf(eight)
    ) = MedicationWithSchedules(
        medication = Medication(
            id = id,
            name = name,
            dosage = "100 mg",
            form = MedicationForm.PILL,
            notes = "",
            isActive = isActive,
            startDate = today.minusDays(60)
        ),
        schedules = times.map { DoseSchedule(time = it) }
    )

    private fun log(
        medicationId: Long,
        date: LocalDate,
        time: LocalTime = eight,
        status: DoseStatus = DoseStatus.TAKEN
    ) = DoseLog(
        medicationId = medicationId,
        date = date,
        time = time,
        status = status,
        loggedAt = Instant.EPOCH
    )

    @Test
    fun reportCoversTheRequestedDaysInAscendingOrder() {
        val report = useCase(listOf(item()), emptyList(), today, 7, afternoon)
        assertEquals(7, report.days.size)
        assertEquals(today.minusDays(6), report.days.first().date)
        assertEquals(today, report.days.last().date)
    }

    @Test
    fun everyDoseTakenGivesFullRateAndCompleteDays() {
        val logs = (0..2).map { log(1, today.minusDays(it.toLong())) }
        val report = useCase(listOf(item()), logs, today, 3, afternoon)

        assertEquals(1f, report.rate!!, 0.0001f)
        assertEquals(3, report.takenCount)
        assertEquals(true, report.days.all { it.status == DayAdherenceStatus.ALL_TAKEN })
    }

    @Test
    fun anUnloggedPastDoseCountsAsMissed() {
        val logs = listOf(log(1, today.minusDays(2)), log(1, today))
        val report = useCase(listOf(item()), logs, today, 3, afternoon)

        assertEquals(DayAdherenceStatus.NONE_TAKEN, report.days[1].status)
        assertEquals(2, report.takenCount)
        assertEquals(3, report.resolvedCount)
        assertEquals(2f / 3f, report.rate!!, 0.0001f)
    }

    @Test
    fun anUpcomingDoseTodayIsPendingAndNotCounted() {
        val report = useCase(listOf(item()), emptyList(), today, 1, morning)

        assertEquals(DayAdherenceStatus.PENDING, report.days.single().status)
        assertEquals(0, report.resolvedCount)
        assertNull(report.rate)
    }

    @Test
    fun aSkippedDoseCountsAgainstAdherence() {
        val logs = listOf(log(1, today, status = DoseStatus.SKIPPED))
        val report = useCase(listOf(item()), logs, today, 1, afternoon)

        assertEquals(1, report.resolvedCount)
        assertEquals(0, report.takenCount)
        assertEquals(0f, report.rate!!, 0.0001f)
        assertEquals(DayAdherenceStatus.NONE_TAKEN, report.days.single().status)
    }

    @Test
    fun oneOfTwoDosesTakenMakesAPartialDay() {
        val twiceDaily = item(times = arrayOf(eight, twenty))
        val logs = listOf(log(1, today, eight))
        val report = useCase(listOf(twiceDaily), logs, today, 1, lateEvening)

        assertEquals(DayAdherenceStatus.PARTIAL, report.days.single().status)
    }

    @Test
    fun anArchivedMedicationOnlyCountsLoggedDoses() {
        val archived = item(isActive = false)
        val logs = listOf(log(1, today.minusDays(2)))
        val report = useCase(listOf(archived), logs, today, 3, afternoon)

        assertEquals(1, report.takenCount)
        assertEquals(1, report.resolvedCount)
        assertEquals(DayAdherenceStatus.NO_DOSES, report.days[1].status)
        assertEquals(DayAdherenceStatus.NO_DOSES, report.days[2].status)
    }

    @Test
    fun adherencePerMedicationIsComputedSeparatelyAndSortedByName() {
        val aspirin = item(id = 1, name = "Aspirin")
        val zinc = item(id = 2, name = "Zinc")
        val yesterday = today.minusDays(1)
        val logs = listOf(
            log(1, yesterday), log(1, today),
            log(2, today)
        )
        val report = useCase(listOf(zinc, aspirin), logs, today, 2, afternoon)

        assertEquals(listOf("Aspirin", "Zinc"), report.perMedication.map { it.medication.name })
        assertEquals(1f, report.perMedication[0].rate, 0.0001f)
        assertEquals(0.5f, report.perMedication[1].rate, 0.0001f)
    }

    @Test
    fun streakCountsCompleteDaysAndIgnoresAPendingToday() {
        val logs = listOf(log(1, today.minusDays(1)), log(1, today.minusDays(2)))
        val report = useCase(listOf(item()), logs, today, 4, morning)

        // today pending (neutral), yesterday and the day before complete, three days ago missed
        assertEquals(2, report.currentStreakDays)
    }

    @Test
    fun noStreakWhenTheLatestDayWasMissed() {
        val logs = listOf(log(1, today.minusDays(1)))
        val report = useCase(listOf(item()), logs, today, 2, afternoon)

        assertEquals(0, report.currentStreakDays)
    }
}
