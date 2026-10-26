package com.gcoded.meditrack.ui.screens.today

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.gcoded.meditrack.R
import com.gcoded.meditrack.reminder.ReminderPermissions

/** Explains and requests what reminders need; renders nothing once everything is granted. */
@Composable
fun ReminderPermissionBanner(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var notificationsGranted by remember {
        mutableStateOf(ReminderPermissions.notificationsGranted(context))
    }
    var exactAlarmsGranted by remember {
        mutableStateOf(ReminderPermissions.canScheduleExactAlarms(context))
    }
    var notificationPermissionRequested by rememberSaveable { mutableStateOf(false) }

    // The user may change these in system settings, so re-check every time we come back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsGranted = ReminderPermissions.notificationsGranted(context)
        exactAlarmsGranted = ReminderPermissions.canScheduleExactAlarms(context)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        notificationsGranted = ReminderPermissions.notificationsGranted(context)
    }

    if (notificationsGranted && exactAlarmsGranted) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.NotificationsOff, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.reminder_banner_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!notificationsGranted) {
                Text(
                    text = stringResource(R.string.reminder_banner_notifications_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        val canAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !notificationPermissionRequested
                        if (canAsk) {
                            notificationPermissionRequested = true
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openNotificationSettings(context)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reminder_banner_allow_notifications))
                }
            }

            if (!exactAlarmsGranted) {
                Text(
                    text = stringResource(R.string.reminder_banner_exact_alarms_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { openExactAlarmSettings(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reminder_banner_allow_exact_alarms))
                }
            }
        }
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    context.startActivity(intent)
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            Uri.fromParts("package", context.packageName, null)
        )
        context.startActivity(intent)
    }
}
