package com.gcoded.meditrack.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import com.gcoded.meditrack.domain.repository.MedicationRepository
import com.gcoded.meditrack.domain.usecase.BuildAdherenceReportUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val buildAdherenceReport: BuildAdherenceReportUseCase,
    private val pdfGenerator: AdherenceReportPdfGenerator,
    private val clock: Clock
) {

    /** Builds the report for the last [days] days and returns a shareable content URI. */
    suspend fun export(days: Int): Uri = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now(clock)
        val today = now.toLocalDate()
        val start = today.minusDays(days - 1L)

        val medications = medicationRepository.observeMedications().first()
        val logs = doseLogRepository.observeLogsBetween(start, today).first()
        val report = buildAdherenceReport(medications, logs, today, days, now)

        // Health data: keep a single report in the private cache, never in public storage.
        val directory = File(context.cacheDir, "reports").apply { mkdirs() }
        directory.listFiles()?.forEach { it.delete() }
        val file = File(directory, "meditrack-report-$today-${days}d.pdf")
        file.outputStream().use { pdfGenerator.write(report, today, it) }

        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
