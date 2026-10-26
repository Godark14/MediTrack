package com.gcoded.meditrack.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class DoseSchedule(
    val id: Long = 0,
    val medicationId: Long = 0,
    val time: LocalTime,
    val daysOfWeek: Set<DayOfWeek> = DayOfWeek.entries.toSet()
)
