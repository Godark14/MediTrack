package com.gcoded.meditrack.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import com.gcoded.meditrack.domain.repository.MedicationRepository
import com.gcoded.meditrack.domain.usecase.FindNextReminderUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps exactly one alarm armed: the one for the next upcoming dose.
 * Calling [reschedule] again simply replaces it.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val findNextReminder: FindNextReminderUseCase,
    private val clock: Clock
) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    suspend fun reschedule() {
        val now = LocalDateTime.now(clock)
        val today = now.toLocalDate()
        val medications = medicationRepository.observeMedications().first()
        val logs = doseLogRepository
            .observeLogsBetween(today, today.plusDays(FindNextReminderUseCase.LOOKAHEAD_DAYS.toLong()))
            .first()

        val trigger = findNextReminder(medications, logs, now)
        if (trigger == null) {
            alarmManager.cancel(alarmIntent(0L))
            return
        }

        val triggerMillis = trigger.triggerAt.atZone(clock.zone).toInstant().toEpochMilli()
        val pendingIntent = alarmIntent(triggerMillis)

        if (ReminderPermissions.canScheduleExactAlarms(context)) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                return
            } catch (_: SecurityException) {
                // Permission was revoked between the check and the call: fall back below.
            }
        }
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
    }

    private fun alarmIntent(scheduledAtMillis: Long): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java)
            .putExtra(ReminderReceiver.EXTRA_SCHEDULED_AT_MILLIS, scheduledAtMillis)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
