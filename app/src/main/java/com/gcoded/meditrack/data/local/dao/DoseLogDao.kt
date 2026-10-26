package com.gcoded.meditrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gcoded.meditrack.data.local.entity.DoseLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {

    @Query("SELECT * FROM dose_logs WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeBetween(startEpochDay: Long, endEpochDay: Long): Flow<List<DoseLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: DoseLogEntity)

    @Query(
        "DELETE FROM dose_logs WHERE medicationId = :medicationId " +
            "AND epochDay = :epochDay AND minuteOfDay = :minuteOfDay"
    )
    suspend fun deleteSlot(medicationId: Long, epochDay: Long, minuteOfDay: Int)
}
