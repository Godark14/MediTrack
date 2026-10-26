package com.gcoded.meditrack.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class DoseLog(
    val id: Long = 0,
    val medicationId: Long,
    val date: LocalDate,
    val time: LocalTime,
    val status: DoseStatus,
    val loggedAt: Instant
)
