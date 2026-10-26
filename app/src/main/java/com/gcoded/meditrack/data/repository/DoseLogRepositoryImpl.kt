package com.gcoded.meditrack.data.repository

import com.gcoded.meditrack.data.local.dao.DoseLogDao
import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class DoseLogRepositoryImpl @Inject constructor(
    private val doseLogDao: DoseLogDao
) : DoseLogRepository {

    override fun observeLogsBetween(start: LocalDate, end: LocalDate): Flow<List<DoseLog>> =
        doseLogDao.observeBetween(start.toEpochDay(), end.toEpochDay())
            .map { list -> list.map { it.toDomain() } }

    override suspend fun logDose(
        medicationId: Long,
        date: LocalDate,
        time: LocalTime,
        status: DoseStatus
    ) {
        doseLogDao.upsert(
            DoseLog(
                medicationId = medicationId,
                date = date,
                time = time,
                status = status,
                loggedAt = Instant.now()
            ).toEntity()
        )
    }

    override suspend fun undoDose(medicationId: Long, date: LocalDate, time: LocalTime) {
        doseLogDao.deleteSlot(
            medicationId = medicationId,
            epochDay = date.toEpochDay(),
            minuteOfDay = time.hour * 60 + time.minute
        )
    }
}
