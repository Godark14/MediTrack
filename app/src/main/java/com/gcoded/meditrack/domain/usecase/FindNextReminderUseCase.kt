package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import com.gcoded.meditrack.domain.model.ReminderTrigger
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Pure business logic: finds the next upcoming, not-yet-logged dose slot after [now]
 * (looking [LOOKAHEAD_DAYS] days ahead) and groups every slot due at that same moment.
 */
class FindNextReminderUseCase @Inject constructor(
    private val buildDaySlots: BuildDaySlotsUseCase
) {

    operator fun invoke(
        medications: List<MedicationWithSchedules>,
        logs: List<DoseLog>,
        now: LocalDateTime
    ): ReminderTrigger? {
        val today = now.toLocalDate()
        for (offset in 0..LOOKAHEAD_DAYS) {
            val date = today.plusDays(offset.toLong())
            val upcoming = buildDaySlots(date, medications, logs, now)
                .filter { it.status == DoseSlotStatus.UPCOMING }
            if (upcoming.isNotEmpty()) {
                val firstTime = upcoming.minOf { it.time }
                return ReminderTrigger(
                    triggerAt = date.atTime(firstTime),
                    slots = upcoming.filter { it.time == firstTime }
                )
            }
        }
        return null
    }

    companion object {
        const val LOOKAHEAD_DAYS = 8
    }
}
