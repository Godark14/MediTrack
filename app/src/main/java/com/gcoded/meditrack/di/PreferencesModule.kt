package com.gcoded.meditrack.di

import com.gcoded.meditrack.data.preferences.DataStoreUserPreferencesRepository
import com.gcoded.meditrack.data.repository.AppDataRepositoryImpl
import com.gcoded.meditrack.domain.repository.AppDataRepository
import com.gcoded.meditrack.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: DataStoreUserPreferencesRepository
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAppDataRepository(impl: AppDataRepositoryImpl): AppDataRepository
}