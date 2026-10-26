package com.gcoded.meditrack.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/** Handles the Taken / Skip buttons of a reminder notification. */
@AndroidEntryPoint
class DoseActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var doseLogRepository: DoseLogRepository

    @Inject
    lateinit var notifier: DoseNotifier

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_LOG_DOSE) return

        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val epochDay = intent.getLongExtra(EXTRA_EPOCH_DAY, Long.MIN_VALUE)
        val minuteOfDay = intent.getIntExtra(EXTRA_MINUTE_OF_DAY, -1)
        val status = intent.getStringExtra(EXTRA_STATUS)?.let(DoseStatus::fromName) ?: return
        if (medicationId < 0L || epochDay == Long.MIN_VALUE || minuteOfDay < 0) return

        notifier.cancel(medicationId, epochDay, minuteOfDay)
        launchAsync {
            doseLogRepository.logDose(
                medicationId = medicationId,
                date = LocalDate.ofEpochDay(epochDay),
                time = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60),
                status = status
            )
        }
    }

    companion object {
        const val ACTION_LOG_DOSE = "com.gcoded.meditrack.action.LOG_DOSE"
        const val EXTRA_MEDICATION_ID = "medication_id"
        const val EXTRA_EPOCH_DAY = "epoch_day"
        const val EXTRA_MINUTE_OF_DAY = "minute_of_day"
        const val EXTRA_STATUS = "status"
    }
}
