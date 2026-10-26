package com.gcoded.meditrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dose_logs",
    foreignKeys = [
        ForeignKey(
            entity = MedicationEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["medicationId", "epochDay", "minuteOfDay"], unique = true),
        Index("epochDay")
    ]
)
data class DoseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val epochDay: Long,
    val minuteOfDay: Int,
    val status: String,
    val loggedAtEpochMillis: Long
)
