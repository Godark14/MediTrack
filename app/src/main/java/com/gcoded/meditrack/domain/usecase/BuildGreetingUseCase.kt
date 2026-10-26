package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.DaySummary
import com.gcoded.meditrack.domain.model.DayPeriod
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.model.Greeting
import com.gcoded.meditrack.domain.model.TodayMessage
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Pure business logic: picks the salutation from the time of day and the contextual
 * message from the state of today's doses. The first matching rule wins.
 */
class BuildGreetingUseCase @Inject constructor() {

    operator fun invoke(summary: DaySummary, now: LocalDateTime, userName: String): Greeting =
        Greeting(
            period = DayPeriod.at(now.toLocalTime()),
            userName = userName.trim(),
            message = messageFor(summary, now)
        )

    private fun messageFor(summary: DaySummary, now: LocalDateTime): TodayMessage {
        val slots = summary.slots
        if (slots.isEmpty()) return TodayMessage.NoDosesToday

        val taken = slots.count { it.status == DoseSlotStatus.TAKEN }
        val skipped = slots.count { it.status == DoseSlotStatus.SKIPPED }
        val missed = slots.count { it.status == DoseSlotStatus.MISSED }
        val due = slots.count { it.status == DoseSlotStatus.DUE }
        val upcoming = slots.filter { it.status == DoseSlotStatus.UPCOMING }

        return when {
            due + missed + upcoming.size == 0 ->
                if (taken > 0) {
                    TodayMessage.AllDone(isLate = now.hour >= LATE_HOUR || now.hour < 5)
                } else {
                    TodayMessage.AllSkipped
                }
            taken == 0 && skipped == 0 && due + missed > 0 -> TodayMessage.CheckIn
            due > 0 -> TodayMessage.DueNow(due)
            missed > 0 -> TodayMessage.Missed(missed)
            else -> {
                val next = upcoming.minBy { it.time }
                TodayMessage.NextDose(next.time, next.medication.name)
            }
        }
    }

    private companion object {
        const val LATE_HOUR = 21
    }
}