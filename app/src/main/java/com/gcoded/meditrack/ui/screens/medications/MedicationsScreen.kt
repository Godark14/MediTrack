package com.gcoded.meditrack.ui.screens.medications

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MedicationsScreen(
    onAddClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: MedicationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    val undoLabel = stringResource(R.string.undo_label)
    var pendingDelete by remember { mutableStateOf<Medication?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is MedicationsEvent.ActiveChanged -> {
                    val messageRes = if (event.isActive) {
                        R.string.medication_restored_message
                    } else {
                        R.string.medication_archived_message
                    }
                    val result = snackbarHostState.showSnackbar(
                        message = resources.getString(messageRes, event.medication.name),
                        actionLabel = undoLabel,
                        withDismissAction = true
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onUndoActiveChange(event.medication, !event.isActive)
                    }
                }
                is MedicationsEvent.Deleted -> {
                    snackbarHostState.showSnackbar(
                        message = resources.getString(
                            R.string.medication_deleted_message,
                            event.medication.name
                        ),
                        withDismissAction = true
                    )
                }
            }
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val subtitle = when (val state = uiState) {
        is MedicationsUiState.Content -> stringResource(
            R.string.medications_subtitle_format,
            state.active.size,
            state.archived.size
        )
        else -> stringResource(R.string.medications_subtitle_default)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.medications_title)) },
                subtitle = { Text(subtitle) },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_medication_fab)) }
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            MedicationsUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
            MedicationsUiState.Empty -> EmptyState(innerPadding)
            is MedicationsUiState.Content -> MedicationsList(
                state = state,
                contentPadding = innerPadding,
                onEdit = { onEditClick(it.id) },
                onToggleActive = { viewModel.onSetActive(it, !it.isActive) },
                onDelete = { pendingDelete = it }
            )
        }
    }

    pendingDelete?.let { medication ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.medication_delete_title)) },
            text = {
                Text(stringResource(R.string.medication_delete_message, medication.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDelete(medication)
                        pendingDelete = null
                    }
                ) {
                    Text(stringResource(R.string.medication_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
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
            imageVector = Icons.Filled.Medication,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(R.string.medications_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.medications_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MedicationsList(
    state: MedicationsUiState.Content,
    contentPadding: PaddingValues,
    onEdit: (Medication) -> Unit,
    onToggleActive: (Medication) -> Unit,
    onDelete: (Medication) -> Unit
) {
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
        medicationSection(R.string.medications_section_active, state.active, onEdit, onToggleActive, onDelete)
        medicationSection(R.string.medications_section_archived, state.archived, onEdit, onToggleActive, onDelete)
    }
}

private fun LazyListScope.medicationSection(
    @StringRes titleRes: Int,
    items: List<MedicationWithSchedules>,
    onEdit: (Medication) -> Unit,
    onToggleActive: (Medication) -> Unit,
    onDelete: (Medication) -> Unit
) {
    if (items.isEmpty()) return

    item(key = "header-$titleRes") {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )
    }
    itemsIndexed(items, key = { _, item -> "medication-${item.medication.id}" }) { index, item ->
        MedicationListItem(
            item = item,
            index = index,
            count = items.size,
            onClick = { onEdit(item.medication) },
            onToggleActive = { onToggleActive(item.medication) },
            onDelete = { onDelete(item.medication) },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .animateItem()
        )
    }
}
