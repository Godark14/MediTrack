package com.gcoded.meditrack.ui.screens.history

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.DayAdherence
import com.gcoded.meditrack.domain.model.DayAdherenceStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

/** Week-aligned grid (Monday first) with one colored cell per day. */
@Composable
fun AdherenceCalendar(
    days: List<DayAdherence>,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekdayLabels = remember {
        DayOfWeek.entries.map { it.getDisplayName(TextStyle.NARROW, Locale.getDefault()) }
    }
    val leadingEmptyCells = days.first().date.dayOfWeek.value - 1
    val weeks = (List<DayAdherence?>(leadingEmptyCells) { null } + days).chunked(7)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekdayLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (index in 0 until 7) {
                    val day = week.getOrNull(index)
                    if (day == null) {
                        Spacer(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        DayCell(
                            day = day,
                            isSelected = day.date == selectedDate,
                            onClick = { onDayClick(day.date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: DayAdherence,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (container, content) = day.status.cellColors()
    val statusLabel = stringResource(day.status.labelRes())
    val dateText = remember(day.date) {
        day.date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
    val description = stringResource(
        R.string.history_day_content_description_format,
        dateText,
        statusLabel
    )
    val shape = MaterialTheme.shapes.medium

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(container)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, shape)
                } else {
                    Modifier
                }
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                this.selected = isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = content
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarLegend(modifier: Modifier = Modifier) {
    val legendStatuses = listOf(
        DayAdherenceStatus.ALL_TAKEN,
        DayAdherenceStatus.PARTIAL,
        DayAdherenceStatus.NONE_TAKEN,
        DayAdherenceStatus.PENDING
    )
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        legendStatuses.forEach { status ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(status.cellColors().first)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(status.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DayAdherenceStatus.cellColors(): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        DayAdherenceStatus.ALL_TAKEN -> scheme.primary to scheme.onPrimary
        DayAdherenceStatus.PARTIAL -> scheme.tertiary to scheme.onTertiary
        DayAdherenceStatus.NONE_TAKEN -> scheme.error to scheme.onError
        DayAdherenceStatus.PENDING -> scheme.secondaryContainer to scheme.onSecondaryContainer
        DayAdherenceStatus.NO_DOSES -> scheme.surfaceContainerHigh to scheme.onSurfaceVariant
    }
}

@StringRes
private fun DayAdherenceStatus.labelRes(): Int = when (this) {
    DayAdherenceStatus.ALL_TAKEN -> R.string.history_status_complete
    DayAdherenceStatus.PARTIAL -> R.string.history_status_partial
    DayAdherenceStatus.NONE_TAKEN -> R.string.history_status_missed
    DayAdherenceStatus.PENDING -> R.string.history_status_pending
    DayAdherenceStatus.NO_DOSES -> R.string.history_status_none
}
