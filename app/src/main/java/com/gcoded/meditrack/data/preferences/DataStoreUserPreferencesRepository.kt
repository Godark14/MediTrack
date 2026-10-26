package com.gcoded.meditrack.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gcoded.meditrack.domain.model.ThemeMode
import com.gcoded.meditrack.domain.model.UserPreferences
import com.gcoded.meditrack.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Singleton
class DataStoreUserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object Keys {
        val USER_NAME = stringPreferencesKey("user_name")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    }

    override val preferences: Flow<UserPreferences> = context.userPreferencesDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            UserPreferences(
                userName = prefs[Keys.USER_NAME].orEmpty(),
                onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
                themeMode = ThemeMode.fromName(prefs[Keys.THEME_MODE]),
                useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true
            )
        }

    override suspend fun completeOnboarding(name: String) {
        context.userPreferencesDataStore.edit {
            it[Keys.USER_NAME] = name.trim()
            it[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun setUserName(name: String) {
        context.userPreferencesDataStore.edit { it[Keys.USER_NAME] = name.trim() }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.userPreferencesDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        context.userPreferencesDataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun clear() {
        context.userPreferencesDataStore.edit { it.clear() }
    }
}