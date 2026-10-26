package com.gcoded.meditrack.ui.screens.today

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DoseSlotItem(
    slot: DoseSlot,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    showActions: Boolean = true,
    onTaken: () -> Unit = {},
    onSkipped: () -> Unit = {}
) {
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    val statusLabel = stringResource(slot.status.labelRes())
    val isActionable = showActions &&
        (slot.status == DoseSlotStatus.DUE || slot.status == DoseSlotStatus.MISSED)
    val takenLabel = stringResource(R.string.dose_action_taken)
    val skipLabel = stringResource(R.string.dose_action_skip)

    val containerColor = when (slot.status) {
        DoseSlotStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
        DoseSlotStatus.DUE -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = containerColor),
        leadingContent = {
            Text(
                text = slot.time.format(timeFormatter),
                style = MaterialTheme.typography.titleMedium
            )
        },
        trailingContent = {
            Icon(
                imageVector = slot.status.icon(),
                contentDescription = statusLabel,
                tint = slot.status.iconTint()
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = stringResource(
                        R.string.dose_supporting_format,
                        slot.medication.dosage,
                        statusLabel
                    )
                )
                if (isActionable) {
                    Spacer(Modifier.height(8.dp))
                    ButtonGroup(
                        overflowIndicator = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        clickableItem(onClick = onTaken, label = takenLabel, weight = 1f)
                        clickableItem(onClick = onSkipped, label = skipLabel, weight = 1f)
                    }
                }
            }
        },
        content = {
            Text(
                text = slot.medication.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
}

private fun DoseSlotStatus.labelRes(): Int = when (this) {
    DoseSlotStatus.UPCOMING -> R.string.dose_status_upcoming
    DoseSlotStatus.DUE -> R.string.dose_status_due
    DoseSlotStatus.MISSED -> R.string.dose_status_missed
    DoseSlotStatus.TAKEN -> R.string.dose_status_taken
    DoseSlotStatus.SKIPPED -> R.string.dose_status_skipped
}

private fun DoseSlotStatus.icon(): ImageVector = when (this) {
    DoseSlotStatus.UPCOMING -> Icons.Filled.Schedule
    DoseSlotStatus.DUE -> Icons.Filled.NotificationsActive
    DoseSlotStatus.MISSED -> Icons.Filled.Warning
    DoseSlotStatus.TAKEN -> Icons.Filled.CheckCircle
    DoseSlotStatus.SKIPPED -> Icons.Filled.SkipNext
}

@Composable
private fun DoseSlotStatus.iconTint(): Color = when (this) {
    DoseSlotStatus.MISSED -> MaterialTheme.colorScheme.error
    DoseSlotStatus.DUE, DoseSlotStatus.TAKEN -> MaterialTheme.colorScheme.primary
    DoseSlotStatus.UPCOMING, DoseSlotStatus.SKIPPED -> MaterialTheme.colorScheme.onSurfaceVariant
}
