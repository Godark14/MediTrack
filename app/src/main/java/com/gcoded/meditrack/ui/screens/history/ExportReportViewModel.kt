package com.gcoded.meditrack.ui.screens.history

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcoded.meditrack.export.ReportExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExportEvent {
    data class Ready(val uri: Uri) : ExportEvent
    data object Failed : ExportEvent
}

@HiltViewModel
class ExportReportViewModel @Inject constructor(
    private val reportExporter: ReportExporter
) : ViewModel() {

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _events = Channel<ExportEvent>(Channel.BUFFERED)
    val events: Flow<ExportEvent> = _events.receiveAsFlow()

    fun onExportClick(range: HistoryRange) {
        if (_isExporting.value) return
        _isExporting.value = true
        viewModelScope.launch {
            try {
                _events.send(ExportEvent.Ready(reportExporter.export(range.days)))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _events.send(ExportEvent.Failed)
            } finally {
                _isExporting.value = false
            }
        }
    }
}
