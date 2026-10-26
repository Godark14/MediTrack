package com.gcoded.meditrack.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Re-arms the alarm after events that clear or invalidate it: reboot, app update,
 * clock or time zone change, and the exact-alarm permission being granted or revoked.
 */
@AndroidEntryPoint
class SystemEventReceiver : BroadcastReceiver() {

    @Inject
    lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        launchAsync { scheduler.reschedule() }
    }
}
