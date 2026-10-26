package com.gcoded.meditrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gcoded.meditrack.data.local.entity.MedicationEntity
import com.gcoded.meditrack.data.local.entity.MedicationWithSchedulesRelation
import com.gcoded.meditrack.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MedicationDao {

    @Transaction
    @Query("SELECT * FROM medications ORDER BY name COLLATE NOCASE ASC")
    abstract fun observeAll(): Flow<List<MedicationWithSchedulesRelation>>

    @Transaction
    @Query("SELECT * FROM medications WHERE id = :id")
    abstract suspend fun getById(id: Long): MedicationWithSchedulesRelation?

    @Insert
    abstract suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    abstract suspend fun updateMedication(medication: MedicationEntity)

    @Insert
    abstract suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Query("DELETE FROM schedules WHERE medicationId = :medicationId")
    abstract suspend fun deleteSchedulesFor(medicationId: Long)

    @Query("UPDATE medications SET isActive = :isActive WHERE id = :id")
    abstract suspend fun setActive(id: Long, isActive: Boolean)

    @Query("DELETE FROM medications WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Transaction
    open suspend fun saveWithSchedules(
        medication: MedicationEntity,
        schedules: List<ScheduleEntity>
    ): Long {
        val id = if (medication.id == 0L) {
            insertMedication(medication)
        } else {
            updateMedication(medication)
            medication.id
        }
        deleteSchedulesFor(id)
        insertSchedules(schedules.map { it.copy(id = 0, medicationId = id) })
        return id
    }
}
