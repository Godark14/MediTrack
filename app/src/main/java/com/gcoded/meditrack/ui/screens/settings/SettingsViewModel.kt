package com.gcoded.meditrack.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcoded.meditrack.domain.model.ThemeMode
import com.gcoded.meditrack.domain.model.UserPreferences
import com.gcoded.meditrack.domain.repository.AppDataRepository
import com.gcoded.meditrack.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appDataRepository: AppDataRepository
) : ViewModel() {

    /** Null until the first emission, so the screen can show a loader instead of default values. */
    val preferences: StateFlow<UserPreferences?> = userPreferencesRepository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun onNameChanged(name: String) {
        viewModelScope.launch { userPreferencesRepository.setUserName(name) }
    }

    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { userPreferencesRepository.setThemeMode(mode) }
    }

    fun onDynamicColorChanged(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setDynamicColor(enabled) }
    }

    fun onDeleteAllData() {
        viewModelScope.launch { appDataRepository.deleteAllData() }
    }
}