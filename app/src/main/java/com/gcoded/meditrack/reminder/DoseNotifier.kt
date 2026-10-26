package com.gcoded.meditrack.reminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.gcoded.meditrack.MainActivity
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Objects
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DoseNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

    // Permission is verified through areNotificationsEnabled() just below.
    @SuppressLint("MissingPermission")
    fun show(slot: DoseSlot) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val minuteOfDay = slot.time.hour * 60 + slot.time.minute
        val id = notificationId(slot.medication.id, slot.date.toEpochDay(), minuteOfDay)

        // Medication names are sensitive: the lock screen only shows a generic message.
        val publicVersion = NotificationCompat.Builder(context, NotificationChannels.DOSE_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_public_title))
            .setContentText(context.getString(R.string.notification_public_text))
            .build()

        val notification = NotificationCompat.Builder(context, NotificationChannels.DOSE_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title_format, slot.medication.name))
            .setContentText(
                context.getString(
                    R.string.notification_text_format,
                    slot.medication.dosage,
                    slot.time.format(timeFormatter)
                )
            )
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .addAction(
                0,
                context.getString(R.string.notification_action_taken),
                actionIntent(slot, minuteOfDay, DoseStatus.TAKEN)
            )
            .addAction(
                0,
                context.getString(R.string.notification_action_skip),
                actionIntent(slot, minuteOfDay, DoseStatus.SKIPPED)
            )
            .build()

        manager.notify(id, notification)
    }

    fun cancel(slot: DoseSlot) {
        cancel(slot.medication.id, slot.date.toEpochDay(), slot.time.hour * 60 + slot.time.minute)
    }

    fun cancel(medicationId: Long, epochDay: Long, minuteOfDay: Int) {
        NotificationManagerCompat.from(context)
            .cancel(notificationId(medicationId, epochDay, minuteOfDay))
    }

    private fun openAppIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun actionIntent(slot: DoseSlot, minuteOfDay: Int, status: DoseStatus): PendingIntent {
        val epochDay = slot.date.toEpochDay()
        val intent = Intent(context, DoseActionReceiver::class.java).apply {
            action = DoseActionReceiver.ACTION_LOG_DOSE
            // A unique data URI keeps every slot/action PendingIntent distinct.
            data = Uri.parse("meditrack://dose/${slot.medication.id}/$epochDay/$minuteOfDay/${status.name}")
            putExtra(DoseActionReceiver.EXTRA_MEDICATION_ID, slot.medication.id)
            putExtra(DoseActionReceiver.EXTRA_EPOCH_DAY, epochDay)
            putExtra(DoseActionReceiver.EXTRA_MINUTE_OF_DAY, minuteOfDay)
            putExtra(DoseActionReceiver.EXTRA_STATUS, status.name)
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        fun notificationId(medicationId: Long, epochDay: Long, minuteOfDay: Int): Int =
            Objects.hash(medicationId, epochDay, minuteOfDay)
    }
}
