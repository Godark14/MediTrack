package com.gcoded.meditrack

import android.app.Application
import com.gcoded.meditrack.reminder.NotificationChannels
import com.gcoded.meditrack.reminder.ReminderSyncer
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MediTrackApp : Application() {

    @Inject
    lateinit var reminderSyncer: ReminderSyncer

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.create(this)
        reminderSyncer.start()
    }
}
