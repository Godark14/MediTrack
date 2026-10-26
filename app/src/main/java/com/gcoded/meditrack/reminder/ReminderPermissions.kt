package com.gcoded.meditrack.reminder

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat

object ReminderPermissions {

    fun notificationsGranted(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun canScheduleExactAlarms(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
}
