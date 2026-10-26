package com.gcoded.meditrack.di

import com.gcoded.meditrack.data.repository.DoseLogRepositoryImpl
import com.gcoded.meditrack.data.repository.MedicationRepositoryImpl
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import com.gcoded.meditrack.domain.repository.MedicationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(impl: MedicationRepositoryImpl): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindDoseLogRepository(impl: DoseLogRepositoryImpl): DoseLogRepository
}
