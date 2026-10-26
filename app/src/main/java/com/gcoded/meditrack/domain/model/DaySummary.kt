package com.gcoded.meditrack.domain.model

import java.time.LocalDate

data class DaySummary(
    val date: LocalDate,
    val slots: List<DoseSlot>
) {
    val totalCount: Int get() = slots.size
    val takenCount: Int get() = slots.count { it.status == DoseSlotStatus.TAKEN }
    val progress: Float
        get() = if (totalCount == 0) 0f else takenCount.toFloat() / totalCount
}
