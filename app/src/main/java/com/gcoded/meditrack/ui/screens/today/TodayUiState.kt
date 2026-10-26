package com.gcoded.meditrack.ui.screens.today

import com.gcoded.meditrack.domain.model.DaySummary
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.model.Greeting
import java.time.LocalDate

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data class Empty(val date: LocalDate, val greeting: Greeting) : TodayUiState
    data class Content(val summary: DaySummary, val greeting: Greeting) : TodayUiState
}

sealed interface TodayEvent {
    data class DoseLogged(val slot: DoseSlot, val status: DoseStatus) : TodayEvent
}