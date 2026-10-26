package com.gcoded.meditrack.reminder

import android.content.BroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Runs [block] off the main thread while keeping the receiver alive until it finishes. */
internal fun BroadcastReceiver.launchAsync(block: suspend () -> Unit) {
    val pendingResult = goAsync()
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}
