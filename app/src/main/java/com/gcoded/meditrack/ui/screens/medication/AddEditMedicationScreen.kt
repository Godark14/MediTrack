package com.gcoded.meditrack.ui.screens.medication

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.MedicationForm
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale

private data class TimeDialogTarget(val index: Int?, val initial: LocalTime)

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun AddEditMedicationScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditMedicationViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsStateWithLifecycle()
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()

    LaunchedEffect(isSaved) {
        if (isSaved) onNavigateBack()
    }

    var timeDialog by remember { mutableStateOf<TimeDialogTarget?>(null) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditing) R.string.edit_medication_title
                            else R.string.add_medication_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back_content_description)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(R.string.medication_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.dosage,
                onValueChange = viewModel::onDosageChange,
                label = { Text(stringResource(R.string.medication_dosage_label)) },
                supportingText = { Text(stringResource(R.string.medication_dosage_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            SectionTitle(stringResource(R.string.medication_form_label))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MedicationForm.entries.forEach { form ->
                    ToggleButton(
                        checked = state.form == form,
                        onCheckedChange = { checked ->
                            if (checked) viewModel.onFormSelected(form)
                        }
                    ) {
                        Text(stringResource(form.labelRes()))
                    }
                }
            }

            SectionTitle(stringResource(R.string.medication_times_label))
            state.times.forEachIndexed { index, time ->
                TimeRow(
                    text = time.format(timeFormatter),
                    onClick = { timeDialog = TimeDialogTarget(index, time) },
                    onRemove = { viewModel.onTimeRemoved(index) }
                )
            }
            if (!state.hasTimes) {
                ErrorText(stringResource(R.string.medication_times_error))
            }
            FilledTonalButton(
                onClick = {
                    val suggested = state.times.lastOrNull()?.plusHours(4) ?: LocalTime.of(8, 0)
                    timeDialog = TimeDialogTarget(index = null, initial = suggested)
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.medication_add_time))
            }

            SectionTitle(stringResource(R.string.medication_days_label))
            ButtonGroup(
                overflowIndicator = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                DayOfWeek.entries.forEach { day ->
                    toggleableItem(
                        checked = day in state.days,
                        label = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        onCheckedChange = { viewModel.onDayToggled(day, it) },
                        weight = 1f
                    )
                }
            }
            if (!state.hasDays) {
                ErrorText(stringResource(R.string.medication_days_error))
            }

            SectionTitle(stringResource(R.string.medication_period_label))
            DateRow(
                label = stringResource(R.string.medication_start_date_label),
                value = state.startDate.format(dateFormatter),
                onClick = { showStartPicker = true }
            )
            DateRow(
                label = stringResource(R.string.medication_end_date_label),
                value = state.endDate?.format(dateFormatter)
                    ?: stringResource(R.string.medication_no_end_date),
                onClick = { showEndPicker = true },
                trailing = if (state.endDate != null) {
                    {
                        TextButton(onClick = { viewModel.onEndDateChanged(null) }) {
                            Text(stringResource(R.string.medication_clear_end_date))
                        }
                    }
                } else {
                    null
                }
            )
            if (!state.isPeriodValid) {
                ErrorText(stringResource(R.string.medication_end_date_error))
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.medication_notes_label)) },
                minLines = 3,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::onSaveClick,
                enabled = state.isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    stringResource(
                        if (state.isEditing) R.string.medication_update_button
                        else R.string.medication_save_button
                    )
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    timeDialog?.let { target ->
        MediTimePickerDialog(
            initial = target.initial,
            onConfirm = { time ->
                if (target.index == null) {
                    viewModel.onTimeAdded(time)
                } else {
                    viewModel.onTimeChanged(target.index, time)
                }
                timeDialog = null
            },
            onDismiss = { timeDialog = null }
        )
    }

    if (showStartPicker) {
        MediDatePickerDialog(
            initial = state.startDate,
            onConfirm = {
                viewModel.onStartDateChanged(it)
                showStartPicker = false
            },
            onDismiss = { showStartPicker = false }
        )
    }

    if (showEndPicker) {
        MediDatePickerDialog(
            initial = state.endDate ?: state.startDate,
            onConfirm = {
                viewModel.onEndDateChanged(it)
                showEndPicker = false
            },
            onDismiss = { showEndPicker = false }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun TimeRow(
    text: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.medication_remove_time_content_description)
                )
            }
        }
    }
}

@Composable
private fun DateRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            trailing?.invoke()
        }
    }
}

@StringRes
private fun MedicationForm.labelRes(): Int = when (this) {
    MedicationForm.PILL -> R.string.medication_form_pill
    MedicationForm.CAPSULE -> R.string.medication_form_capsule
    MedicationForm.LIQUID -> R.string.medication_form_liquid
    MedicationForm.INJECTION -> R.string.medication_form_injection
    MedicationForm.DROPS -> R.string.medication_form_drops
    MedicationForm.OTHER -> R.string.medication_form_other
}
