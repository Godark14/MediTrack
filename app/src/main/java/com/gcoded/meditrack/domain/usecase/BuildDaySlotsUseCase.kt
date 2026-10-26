package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

/**
 * Pure business logic: turns medication schedules + logged doses into the list of
 * dose slots for a given day, each with its status relative to [now].
 */
class BuildDaySlotsUseCase @Inject constructor() {

    operator fun invoke(
        date: LocalDate,
        medications: List<MedicationWithSchedules>,
        logs: List<DoseLog>,
        now: LocalDateTime,
        includeInactive: Boolean = false
    ): List<DoseSlot> {
        val logsBySlot = logs
            .filter { it.date == date }
            .associateBy { it.medicationId to it.time }

        return medications
            .filter { (includeInactive || it.medication.isActive) && it.medication.isInRangeOn(date) }
            .flatMap { item ->
                item.schedules
                    .filter { date.dayOfWeek in it.daysOfWeek }
                    .map { schedule ->
                        val log = logsBySlot[item.medication.id to schedule.time]
                        DoseSlot(
                            medication = item.medication,
                            date = date,
                            time = schedule.time,
                            status = statusFor(date, schedule.time, log, now)
                        )
                    }
            }
            .sortedWith(compareBy<DoseSlot>({ it.time }, { it.medication.name.lowercase() }))
    }

    private fun Medication.isInRangeOn(date: LocalDate): Boolean =
        !date.isBefore(startDate) && (endDate == null || !date.isAfter(endDate))

    private fun statusFor(
        date: LocalDate,
        time: LocalTime,
        log: DoseLog?,
        now: LocalDateTime
    ): DoseSlotStatus {
        if (log != null) {
            return when (log.status) {
                DoseStatus.TAKEN -> DoseSlotStatus.TAKEN
                DoseStatus.SKIPPED -> DoseSlotStatus.SKIPPED
            }
        }
        val slotDateTime = date.atTime(time)
        return when {
            now.isBefore(slotDateTime) -> DoseSlotStatus.UPCOMING
            now.isBefore(slotDateTime.plus(DUE_WINDOW)) -> DoseSlotStatus.DUE
            else -> DoseSlotStatus.MISSED
        }
    }

    companion object {
        val DUE_WINDOW: Duration = Duration.ofHours(2)
    }
}
