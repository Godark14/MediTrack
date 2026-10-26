package com.gcoded.meditrack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcoded.meditrack.domain.model.UserPreferences
import com.gcoded.meditrack.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AppUiState {
    data object Loading : AppUiState
    data class Ready(val preferences: UserPreferences) : AppUiState
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<AppUiState> = userPreferencesRepository.preferences
        .map<UserPreferences, AppUiState> { AppUiState.Ready(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppUiState.Loading
        )

    fun completeOnboarding(name: String) {
        viewModelScope.launch { userPreferencesRepository.completeOnboarding(name) }
    }
}