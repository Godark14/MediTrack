package com.gcoded.meditrack.data.repository

import com.gcoded.meditrack.data.local.dao.MedicationDao
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import com.gcoded.meditrack.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao
) : MedicationRepository {

    override fun observeMedications(): Flow<List<MedicationWithSchedules>> =
        medicationDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getMedication(id: Long): MedicationWithSchedules? =
        medicationDao.getById(id)?.toDomain()

    override suspend fun saveMedication(item: MedicationWithSchedules): Long =
        medicationDao.saveWithSchedules(
            medication = item.medication.toEntity(),
            schedules = item.schedules.map { it.toEntity() }
        )

    override suspend fun setActive(id: Long, isActive: Boolean) {
        medicationDao.setActive(id, isActive)
    }

    override suspend fun deleteMedication(id: Long) {
        medicationDao.deleteById(id)
    }
}
