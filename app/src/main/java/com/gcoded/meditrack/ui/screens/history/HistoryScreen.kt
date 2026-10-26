package com.gcoded.meditrack.ui.screens.history

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.AdherenceReport
import com.gcoded.meditrack.domain.model.MedicationAdherence
import com.gcoded.meditrack.ui.screens.today.DoseSlotItem
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                subtitle = { Text(stringResource(R.string.history_subtitle)) },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExportReportFab(range = (uiState as? HistoryUiState.Content)?.range)
        }
    ) { innerPadding ->
        when (val state = uiState) {
            HistoryUiState.Loading -> LoadingState(innerPadding)
            HistoryUiState.Empty -> EmptyState(innerPadding)
            is HistoryUiState.Content -> HistoryContent(
                state = state,
                contentPadding = innerPadding,
                onRangeSelected = viewModel::onRangeSelected,
                onDaySelected = viewModel::onDaySelected
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun EmptyState(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.History,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.history_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.history_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HistoryContent(
    state: HistoryUiState.Content,
    contentPadding: PaddingValues,
    onRangeSelected: (HistoryRange) -> Unit,
    onDaySelected: (LocalDate) -> Unit
) {
    val report = state.report
    val selectedDay = report.days.firstOrNull { it.date == state.selectedDate }
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        item(key = "range") {
            RangeSelector(
                selected = state.range,
                onSelected = onRangeSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item(key = "summary") {
            AdherenceSummaryCard(
                report = report,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item(key = "calendar-header") {
            SectionHeader(stringResource(R.string.history_section_calendar))
        }
        item(key = "calendar") {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                AdherenceCalendar(
                    days = report.days,
                    selectedDate = state.selectedDate,
                    onDayClick = onDaySelected
                )
                Spacer(Modifier.height(12.dp))
                CalendarLegend()
            }
        }
        item(key = "day-header") {
            SectionHeader(state.selectedDate.format(dateFormatter))
        }
        if (selectedDay == null || selectedDay.slots.isEmpty()) {
            item(key = "day-empty") {
                Text(
                    text = stringResource(R.string.history_day_no_doses),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        } else {
            itemsIndexed(selectedDay.slots, key = { _, slot -> "day-${slot.key}" }) { index, slot ->
                DoseSlotItem(
                    slot = slot,
                    index = index,
                    count = selectedDay.slots.size,
                    showActions = false,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        if (report.perMedication.isNotEmpty()) {
            item(key = "medication-header") {
                SectionHeader(stringResource(R.string.history_section_by_medication))
            }
            itemsIndexed(
                report.perMedication,
                key = { _, item -> "medication-${item.medication.id}" }
            ) { index, item ->
                MedicationAdherenceItem(
                    item = item,
                    index = index,
                    count = report.perMedication.size,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
        item(key = "bottom-space") { Spacer(Modifier.height(96.dp)) }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RangeSelector(
    selected: HistoryRange,
    onSelected: (HistoryRange) -> Unit,
    modifier: Modifier = Modifier
) {
    // Strings are resolved here: the ButtonGroup content lambda is not composable.
    val labels = HistoryRange.entries.associateWith { stringResource(it.labelRes) }

    ButtonGroup(
        overflowIndicator = {},
        modifier = modifier.fillMaxWidth()
    ) {
        HistoryRange.entries.forEach { option ->
            toggleableItem(
                checked = option == selected,
                label = labels.getValue(option),
                onCheckedChange = { checked -> if (checked) onSelected(option) },
                weight = 1f
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AdherenceSummaryCard(
    report: AdherenceReport,
    modifier: Modifier = Modifier
) {
    val percentFormat = remember { NumberFormat.getPercentInstance() }
    val rate = report.rate
    val animatedRate by animateFloatAsState(targetValue = rate ?: 0f, label = "adherenceRate")

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularWavyProgressIndicator(
                    progress = { animatedRate },
                    modifier = Modifier.size(112.dp)
                )
                Text(
                    text = rate?.let { percentFormat.format(it.toDouble()) } ?: "—",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.history_adherence_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (rate == null) {
                        stringResource(R.string.history_adherence_none)
                    } else {
                        stringResource(
                            R.string.today_progress_format,
                            report.takenCount,
                            report.resolvedCount
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (report.currentStreakDays > 0) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.history_streak_days,
                            report.currentStreakDays,
                            report.currentStreakDays
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MedicationAdherenceItem(
    item: MedicationAdherence,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier
) {
    val percentFormat = remember { NumberFormat.getPercentInstance() }

    SegmentedListItem(
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Medication,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        trailingContent = {
            Text(
                text = percentFormat.format(item.rate.toDouble()),
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = stringResource(
                        R.string.today_progress_format,
                        item.taken,
                        item.resolved
                    )
                )
                LinearWavyProgressIndicator(
                    progress = { item.rate },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        },
        content = {
            Text(
                text = item.medication.name,
                style = MaterialTheme.typography.titleMedium
            )
        }
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}
