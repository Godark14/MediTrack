package com.gcoded.meditrack.domain.repository

import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

interface DoseLogRepository {
    fun observeLogsBetween(start: LocalDate, end: LocalDate): Flow<List<DoseLog>>
    suspend fun logDose(medicationId: Long, date: LocalDate, time: LocalTime, status: DoseStatus)
    suspend fun undoDose(medicationId: Long, date: LocalDate, time: LocalTime)
}
