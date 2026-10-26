package com.gcoded.meditrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val form: String,
    val notes: String,
    val isActive: Boolean,
    val startEpochDay: Long,
    val endEpochDay: Long?
)
