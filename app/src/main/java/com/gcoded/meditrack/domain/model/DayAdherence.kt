package com.gcoded.meditrack.domain.model

import java.time.LocalDate

data class DayAdherence(
    val date: LocalDate,
    val slots: List<DoseSlot>
) {
    val takenCount: Int get() = slots.count { it.status == DoseSlotStatus.TAKEN }
    val skippedCount: Int get() = slots.count { it.status == DoseSlotStatus.SKIPPED }
    val missedCount: Int get() = slots.count { it.status == DoseSlotStatus.MISSED }
    val pendingCount: Int
        get() = slots.count { it.status == DoseSlotStatus.UPCOMING || it.status == DoseSlotStatus.DUE }

    /** Doses whose outcome is known: taken, skipped or missed. */
    val resolvedCount: Int get() = takenCount + skippedCount + missedCount

    val status: DayAdherenceStatus
        get() = when {
            slots.isEmpty() -> DayAdherenceStatus.NO_DOSES
            pendingCount > 0 -> DayAdherenceStatus.PENDING
            takenCount == resolvedCount -> DayAdherenceStatus.ALL_TAKEN
            takenCount == 0 -> DayAdherenceStatus.NONE_TAKEN
            else -> DayAdherenceStatus.PARTIAL
        }
}
