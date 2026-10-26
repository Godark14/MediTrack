package com.gcoded.meditrack.ui.screens.history

import androidx.annotation.StringRes
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.AdherenceReport
import java.time.LocalDate

enum class HistoryRange(val days: Int, @StringRes val labelRes: Int) {
    SEVEN_DAYS(7, R.string.history_range_7_days),
    THIRTY_DAYS(30, R.string.history_range_30_days)
}

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Content(
        val range: HistoryRange,
        val report: AdherenceReport,
        val selectedDate: LocalDate
    ) : HistoryUiState
}
