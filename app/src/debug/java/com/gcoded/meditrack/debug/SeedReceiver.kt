package com.gcoded.meditrack.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gcoded.meditrack.reminder.launchAsync
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Debug builds only: triggered with `adb shell am broadcast -n <package>/<this receiver>`. */
@AndroidEntryPoint
class SeedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var seeder: SampleDataSeeder

    override fun onReceive(context: Context, intent: Intent) {
        launchAsync { seeder.seed() }
    }
}
