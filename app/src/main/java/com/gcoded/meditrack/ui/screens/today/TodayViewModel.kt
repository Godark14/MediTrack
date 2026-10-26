package com.gcoded.meditrack.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import com.gcoded.meditrack.domain.repository.UserPreferencesRepository
import com.gcoded.meditrack.domain.usecase.BuildGreetingUseCase
import com.gcoded.meditrack.domain.usecase.ObserveTodayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    observeToday: ObserveTodayUseCase,
    userPreferencesRepository: UserPreferencesRepository,
    private val buildGreeting: BuildGreetingUseCase,
    private val doseLogRepository: DoseLogRepository,
    private val clock: Clock
) : ViewModel() {

    val uiState: StateFlow<TodayUiState> = combine(
        observeToday(),
        userPreferencesRepository.preferences.map { it.userName }.distinctUntilChanged()
    ) { summary, userName ->
        val greeting = buildGreeting(summary, LocalDateTime.now(clock), userName)
        if (summary.slots.isEmpty()) {
            TodayUiState.Empty(summary.date, greeting)
        } else {
            TodayUiState.Content(summary, greeting)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState.Loading
    )

    private val _events = Channel<TodayEvent>(Channel.BUFFERED)
    val events: Flow<TodayEvent> = _events.receiveAsFlow()

    fun onTaken(slot: DoseSlot) = logDose(slot, DoseStatus.TAKEN)

    fun onSkipped(slot: DoseSlot) = logDose(slot, DoseStatus.SKIPPED)

    fun onUndo(slot: DoseSlot) {
        viewModelScope.launch {
            doseLogRepository.undoDose(slot.medication.id, slot.date, slot.time)
        }
    }

    private fun logDose(slot: DoseSlot, status: DoseStatus) {
        viewModelScope.launch {
            doseLogRepository.logDose(slot.medication.id, slot.date, slot.time, status)
            _events.send(TodayEvent.DoseLogged(slot, status))
        }
    }
}