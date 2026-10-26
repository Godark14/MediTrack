package com.gcoded.meditrack.ui.screens.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MedicationListItem(
    item: MedicationWithSchedules,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val medication = item.medication
    val isArchived = !medication.isActive
    var menuExpanded by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    val timesText = item.schedules.joinToString(", ") { it.time.format(timeFormatter) }
    val days = item.schedules.firstOrNull()?.daysOfWeek.orEmpty()
    val daysText = if (days.size == DayOfWeek.entries.size) {
        stringResource(R.string.medication_every_day)
    } else {
        DayOfWeek.entries
            .filter { it in days }
            .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
    }

    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = if (isArchived) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isArchived) MaterialTheme.colorScheme.surfaceVariant
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Medication,
                    contentDescription = null,
                    tint = if (isArchived) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        supportingContent = {
            Text(
                text = stringResource(
                    R.string.medication_summary_format,
                    medication.dosage,
                    timesText,
                    daysText
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.medication_more_content_description)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.medication_menu_edit)) },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    if (isArchived) R.string.medication_menu_restore
                                    else R.string.medication_menu_archive
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isArchived) Icons.Filled.Unarchive
                                else Icons.Filled.Archive,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onToggleActive()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.medication_menu_delete)) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        },
        content = {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (isArchived) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface
            )
        }
    )
}
