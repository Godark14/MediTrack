package com.gcoded.meditrack.domain.repository

import com.gcoded.meditrack.domain.model.ThemeMode
import com.gcoded.meditrack.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun completeOnboarding(name: String)
    suspend fun setUserName(name: String)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun clear()
}