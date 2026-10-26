package com.gcoded.meditrack.di

import android.content.Context
import androidx.room.Room
import com.gcoded.meditrack.data.local.MediTrackDatabase
import com.gcoded.meditrack.data.local.dao.DoseLogDao
import com.gcoded.meditrack.data.local.dao.MedicationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MediTrackDatabase =
        Room.databaseBuilder(
            context,
            MediTrackDatabase::class.java,
            "meditrack.db"
        ).build()

    @Provides
    fun provideMedicationDao(database: MediTrackDatabase): MedicationDao =
        database.medicationDao()

    @Provides
    fun provideDoseLogDao(database: MediTrackDatabase): DoseLogDao =
        database.doseLogDao()
}
