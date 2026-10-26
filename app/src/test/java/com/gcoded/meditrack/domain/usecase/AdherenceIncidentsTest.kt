package com.gcoded.meditrack.domain.model

import com.gcoded.meditrack.domain.usecase.BuildAdherenceReportUseCase
import com.gcoded.meditrack.domain.usecase.BuildDaySlotsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class AdherenceIncidentsTest {

    private val useCase = BuildAdherenceReportUseCase(BuildDaySlotsUseCase())

    // A Sunday
    private val today = LocalDate.of(2026, 10, 25)
    private val eight = LocalTime.of(8, 0)
    private val twenty = LocalTime.of(20, 0)
    private val lateEvening = today.atTime(23, 0)

    private val twiceDaily = MedicationWithSchedules(
        medication = Medication(
            id = 1,
            name = "Aspirin",
            dosage = "100 mg",
            form = MedicationForm.PILL,
            notes = "",
            startDate = today.minusDays(60)
        ),
        schedules = listOf(DoseSchedule(time = eight), DoseSchedule(time = twenty))
    )

    private fun log(date: LocalDate, time: LocalTime, status: DoseStatus) = DoseLog(
        medicationId = 1,
        date = date,
        time = time,
        status = status,
        loggedAt = Instant.EPOCH
    )

    @Test
    fun noIncidentsWhenEveryDoseWasTaken() {
        val logs = (0..1).flatMap { offset ->
            val date = today.minusDays(offset.toLong())
            listOf(log(date, eight, DoseStatus.TAKEN), log(date, twenty, DoseStatus.TAKEN))
        }
        val report = useCase(listOf(twiceDaily), logs, today, 2, lateEvening)

        assertTrue(report.incidents.isEmpty())
    }

    @Test
    fun missedAndSkippedDosesAreListedChronologically() {
        val twoDaysAgo = today.minusDays(2)
        val logs = listOf(
            log(twoDaysAgo, eight, DoseStatus.TAKEN),
            log(twoDaysAgo, twenty, DoseStatus.SKIPPED),
            // yesterday: nothing logged, so both doses count as missed
            log(today, eight, DoseStatus.TAKEN),
            log(today, twenty, DoseStatus.TAKEN)
        )
        val report = useCase(listOf(twiceDaily), logs, today, 3, lateEvening)

        assertEquals(
            listOf(
                Triple(twoDaysAgo, twenty, DoseSlotStatus.SKIPPED),
                Triple(today.minusDays(1), eight, DoseSlotStatus.MISSED),
                Triple(today.minusDays(1), twenty, DoseSlotStatus.MISSED)
            ),
            report.incidents.map { Triple(it.date, it.time, it.status) }
        )
    }
}
