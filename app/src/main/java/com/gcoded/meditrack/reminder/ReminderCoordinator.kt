package com.gcoded.meditrack.reminder

import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import com.gcoded.meditrack.domain.repository.MedicationRepository
import com.gcoded.meditrack.domain.usecase.BuildDaySlotsUseCase
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderCoordinator @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val buildDaySlots: BuildDaySlotsUseCase,
    private val notifier: DoseNotifier,
    private val scheduler: ReminderScheduler,
    private val clock: Clock
) {

    /** Called when the alarm fires: notify every dose that is due, then arm the next alarm. */
    suspend fun onAlarmFired(scheduledAtMillis: Long) {
        val now = LocalDateTime.now(clock)
        val scheduledAt = if (scheduledAtMillis > 0L) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(scheduledAtMillis), clock.zone)
        } else {
            now.truncatedTo(ChronoUnit.MINUTES)
        }

        val today = now.toLocalDate()
        val medications = medicationRepository.observeMedications().first()
        val logs = doseLogRepository.observeLogsBetween(today, today).first()

        buildDaySlots(today, medications, logs, now)
            .filter { it.status == DoseSlotStatus.DUE && !today.atTime(it.time).isBefore(scheduledAt) }
            .forEach { notifier.show(it) }

        scheduler.reschedule()
    }
}
