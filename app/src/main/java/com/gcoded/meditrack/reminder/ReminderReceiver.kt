package com.gcoded.meditrack.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var coordinator: ReminderCoordinator

    override fun onReceive(context: Context, intent: Intent) {
        val scheduledAtMillis = intent.getLongExtra(EXTRA_SCHEDULED_AT_MILLIS, 0L)
        launchAsync { coordinator.onAlarmFired(scheduledAtMillis) }
    }

    companion object {
        const val EXTRA_SCHEDULED_AT_MILLIS = "scheduled_at_millis"
    }
}
