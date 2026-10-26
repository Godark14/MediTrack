package com.gcoded.meditrack.domain.repository

interface AppDataRepository {
    /** Permanently removes medications, history, exported reports, notifications and preferences. */
    suspend fun deleteAllData()
}