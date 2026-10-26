package com.gcoded.meditrack.domain.repository

import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun observeMedications(): Flow<List<MedicationWithSchedules>>
    suspend fun getMedication(id: Long): MedicationWithSchedules?
    suspend fun saveMedication(item: MedicationWithSchedules): Long
    suspend fun setActive(id: Long, isActive: Boolean)
    suspend fun deleteMedication(id: Long)
}
