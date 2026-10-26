package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.AdherenceReport
import com.gcoded.meditrack.domain.model.DayAdherence
import com.gcoded.meditrack.domain.model.DayAdherenceStatus
import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.model.MedicationAdherence
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Pure business logic: builds the adherence report for the [days] days ending on [endDate].
 *
 * - Adherence = taken / (taken + skipped + missed). Upcoming and due doses are not counted yet.
 * - Archived medications only contribute doses that were actually logged, otherwise
 *   archiving would retroactively create "missed" doses.
 */
class BuildAdherenceReportUseCase @Inject constructor(
    private val buildDaySlots: BuildDaySlotsUseCase
) {

    operator fun invoke(
        medications: List<MedicationWithSchedules>,
        logs: List<DoseLog>,
        endDate: LocalDate,
        days: Int,
        now: LocalDateTime
    ): AdherenceReport {
        val dayList = (days - 1 downTo 0).map { offset ->
            val date = endDate.minusDays(offset.toLong())
            val slots = buildDaySlots(date, medications, logs, now, includeInactive = true)
                .filter { it.medication.isActive || it.status.isLogged() }
            DayAdherence(date = date, slots = slots)
        }

        val allSlots = dayList.flatMap { it.slots }
        val perMedication = allSlots
            .groupBy { it.medication.id }
            .mapNotNull { (_, slots) ->
                val resolved = slots.filter { it.status.isResolved() }
                if (resolved.isEmpty()) {
                    null
                } else {
                    MedicationAdherence(
                        medication = slots.first().medication,
                        taken = resolved.count { it.status == DoseSlotStatus.TAKEN },
                        resolved = resolved.size
                    )
                }
            }
            .sortedBy { it.medication.name.lowercase() }

        return AdherenceReport(
            days = dayList,
            takenCount = allSlots.count { it.status == DoseSlotStatus.TAKEN },
            resolvedCount = allSlots.count { it.status.isResolved() },
            perMedication = perMedication,
            currentStreakDays = currentStreak(dayList)
        )
    }

    private fun currentStreak(days: List<DayAdherence>): Int {
        var streak = 0
        for (day in days.asReversed()) {
            when (day.status) {
                DayAdherenceStatus.ALL_TAKEN -> streak++
                DayAdherenceStatus.NO_DOSES, DayAdherenceStatus.PENDING -> continue
                DayAdherenceStatus.PARTIAL, DayAdherenceStatus.NONE_TAKEN -> return streak
            }
        }
        return streak
    }

    private fun DoseSlotStatus.isLogged(): Boolean =
        this == DoseSlotStatus.TAKEN || this == DoseSlotStatus.SKIPPED

    private fun DoseSlotStatus.isResolved(): Boolean =
        isLogged() || this == DoseSlotStatus.MISSED
}
