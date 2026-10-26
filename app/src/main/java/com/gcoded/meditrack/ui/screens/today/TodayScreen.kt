package com.gcoded.meditrack.ui.screens.today

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.DaySummary
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.model.Greeting
import com.gcoded.meditrack.domain.model.TodayMessage
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TodayScreen(
    onAddMedicationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    val undoLabel = stringResource(R.string.undo_label)

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TodayEvent.DoseLogged -> {
                    val messageRes = when (event.status) {
                        DoseStatus.TAKEN -> R.string.dose_taken_message
                        DoseStatus.SKIPPED -> R.string.dose_skipped_message
                    }
                    val result = snackbarHostState.showSnackbar(
                        message = resources.getString(messageRes, event.slot.medication.name),
                        actionLabel = undoLabel,
                        withDismissAction = true
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndo(event.slot)
                    }
                }
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val greeting: Greeting? = when (val state = uiState) {
        is TodayUiState.Content -> state.greeting
        is TodayUiState.Empty -> state.greeting
        TodayUiState.Loading -> null
    }
    val date = when (val state = uiState) {
        is TodayUiState.Content -> state.summary.date
        is TodayUiState.Empty -> state.date
        TodayUiState.Loading -> LocalDate.now()
    }
    val dateText = remember(date) {
        date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }
    val title = greeting?.titleText() ?: stringResource(R.string.today_title)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                subtitle = { Text(dateText) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.settings_content_description)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMedicationClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_medication_fab)) }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            TodayUiState.Loading -> LoadingState(innerPadding)
            is TodayUiState.Empty -> EmptyState(innerPadding, state.greeting.message)
            is TodayUiState.Content -> TodayList(
                summary = state.summary,
                message = state.greeting.message,
                contentPadding = innerPadding,
                onTaken = viewModel::onTaken,
                onSkipped = viewModel::onSkipped
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
private fun EmptyState(paddingValues: PaddingValues, message: TodayMessage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Medication,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = message.text(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.today_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TodayList(
    summary: DaySummary,
    message: TodayMessage,
    contentPadding: PaddingValues,
    onTaken: (DoseSlot) -> Unit,
    onSkipped: (DoseSlot) -> Unit
) {
    val attention = remember(summary) {
        summary.slots.filter {
            it.status == DoseSlotStatus.DUE || it.status == DoseSlotStatus.MISSED
        }
    }
    val upcoming = remember(summary) {
        summary.slots.filter { it.status == DoseSlotStatus.UPCOMING }
    }
    val done = remember(summary) {
        summary.slots.filter {
            it.status == DoseSlotStatus.TAKEN || it.status == DoseSlotStatus.SKIPPED
        }
    }

    // Extra bottom space so the floating button never hides the last item.
    val listPadding = PaddingValues(
        top = contentPadding.calculateTopPadding(),
        bottom = contentPadding.calculateBottomPadding() + 96.dp
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = listPadding,
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        item(key = "greeting-message") {
            GreetingMessageCard(
                message = message,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
        }
        item(key = "reminder-permissions") {
            ReminderPermissionBanner(modifier = Modifier.padding(horizontal = 16.dp))
        }
        item(key = "progress") {
            ProgressCard(
                summary = summary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
        }
        doseSection(R.string.today_section_attention, attention, onTaken, onSkipped)
        doseSection(R.string.today_section_upcoming, upcoming, onTaken, onSkipped)
        doseSection(R.string.today_section_done, done, onTaken, onSkipped)
    }
}

private fun LazyListScope.doseSection(
    @StringRes titleRes: Int,
    slots: List<DoseSlot>,
    onTaken: (DoseSlot) -> Unit,
    onSkipped: (DoseSlot) -> Unit
) {
    if (slots.isEmpty()) return

    item(key = "header-$titleRes") {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )
    }
    itemsIndexed(slots, key = { _, slot -> slot.key }) { index, slot ->
        DoseSlotItem(
            slot = slot,
            index = index,
            count = slots.size,
            onTaken = { onTaken(slot) },
            onSkipped = { onSkipped(slot) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateItem()
        )
    }
}

@Composable
private fun GreetingMessageCard(
    message: TodayMessage,
    modifier: Modifier = Modifier
) {
    val container = when (message) {
        is TodayMessage.Missed -> MaterialTheme.colorScheme.errorContainer
        is TodayMessage.DueNow, TodayMessage.CheckIn -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = message.icon(), contentDescription = null)
            Spacer(Modifier.width(16.dp))
            Text(
                text = message.text(),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun TodayMessage.icon(): ImageVector = when (this) {
    TodayMessage.NoDosesToday -> Icons.Filled.WbSunny
    is TodayMessage.AllDone -> Icons.Filled.CheckCircle
    TodayMessage.AllSkipped -> Icons.Filled.SkipNext
    TodayMessage.CheckIn -> Icons.Filled.Info
    is TodayMessage.DueNow -> Icons.Filled.NotificationsActive
    is TodayMessage.Missed -> Icons.Filled.Warning
    is TodayMessage.NextDose -> Icons.Filled.Schedule
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProgressCard(
    summary: DaySummary,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = summary.progress,
        label = "dayProgress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(
                    R.string.today_progress_format,
                    summary.takenCount,
                    summary.totalCount
                ),
                style = MaterialTheme.typography.headlineMediumEmphasized
            )
            LinearWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}