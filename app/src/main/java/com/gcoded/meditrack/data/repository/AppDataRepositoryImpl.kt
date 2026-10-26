package com.gcoded.meditrack.data.repository

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.gcoded.meditrack.data.local.MediTrackDatabase
import com.gcoded.meditrack.domain.repository.AppDataRepository
import com.gcoded.meditrack.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: MediTrackDatabase,
    private val userPreferencesRepository: UserPreferencesRepository
) : AppDataRepository {

    override suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            NotificationManagerCompat.from(context).cancelAll()
            File(context.cacheDir, "reports").deleteRecursively()
            // The reminder syncer notices the empty tables and cancels the pending alarm.
            database.clearAllTables()
            // Cleared last: this is what sends the user back to the onboarding screen.
            userPreferencesRepository.clear()
        }
    }
}