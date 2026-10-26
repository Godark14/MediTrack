package com.gcoded.meditrack.domain.model

import java.time.LocalDate
import java.time.LocalTime

data class DoseSlot(
    val medication: Medication,
    val date: LocalDate,
    val time: LocalTime,
    val status: DoseSlotStatus
) {
    val key: String get() = "${medication.id}-${date.toEpochDay()}-$time"
}
