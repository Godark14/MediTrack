package com.gcoded.meditrack.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import com.gcoded.meditrack.domain.repository.MedicationRepository
import com.gcoded.meditrack.domain.usecase.BuildAdherenceReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val buildAdherenceReport: BuildAdherenceReportUseCase,
    private val clock: Clock
) : ViewModel() {

    private val range = MutableStateFlow(HistoryRange.SEVEN_DAYS)
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        range,
        minuteTicker().map { it.toLocalDate() }.distinctUntilChanged()
    ) { selectedRange, today -> selectedRange to today }
        .flatMapLatest { (selectedRange, today) ->
            val start = today.minusDays(selectedRange.days - 1L)
            combine(
                medicationRepository.observeMedications(),
                doseLogRepository.observeLogsBetween(start, today),
                selectedDate,
                minuteTicker()
            ) { medications, logs, selected, now ->
                if (medications.isEmpty()) {
                    HistoryUiState.Empty
                } else {
                    val validSelection = selected
                        ?.takeIf { !it.isBefore(start) && !it.isAfter(today) }
                    HistoryUiState.Content(
                        range = selectedRange,
                        report = buildAdherenceReport(
                            medications = medications,
                            logs = logs,
                            endDate = today,
                            days = selectedRange.days,
                            now = now
                        ),
                        selectedDate = validSelection ?: today
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState.Loading
        )

    fun onRangeSelected(newRange: HistoryRange) {
        range.value = newRange
    }

    fun onDaySelected(date: LocalDate) {
        selectedDate.value = date
    }

    // Ticks every minute so statuses (due -> missed) and the date rollover stay fresh.
    private fun minuteTicker(): Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now(clock))
            delay(60_000L - (clock.millis() % 60_000L))
        }
    }
}
