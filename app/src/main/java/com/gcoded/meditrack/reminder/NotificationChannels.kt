package com.gcoded.meditrack.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.gcoded.meditrack.R

object NotificationChannels {

    const val DOSE_REMINDERS = "dose_reminders"

    fun create(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            DOSE_REMINDERS,
            context.getString(R.string.channel_dose_reminders_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.channel_dose_reminders_description)
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        manager.createNotificationChannel(channel)
    }
}
