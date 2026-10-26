package com.gcoded.meditrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gcoded.meditrack.data.local.dao.DoseLogDao
import com.gcoded.meditrack.data.local.dao.MedicationDao
import com.gcoded.meditrack.data.local.entity.DoseLogEntity
import com.gcoded.meditrack.data.local.entity.MedicationEntity
import com.gcoded.meditrack.data.local.entity.ScheduleEntity

@Database(
    entities = [MedicationEntity::class, ScheduleEntity::class, DoseLogEntity::class],
    version = 1,
    exportSchema = true
)
abstract class MediTrackDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun doseLogDao(): DoseLogDao
}
