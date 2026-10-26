package com.gcoded.meditrack.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.ThemeMode
import com.gcoded.meditrack.domain.model.UserPreferences
import com.gcoded.meditrack.reminder.ReminderPermissions
import java.util.Locale

private const val MAX_NAME_LENGTH = 40

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var notificationsGranted by remember {
        mutableStateOf(ReminderPermissions.notificationsGranted(context))
    }
    var exactAlarmsGranted by remember {
        mutableStateOf(ReminderPermissions.canScheduleExactAlarms(context))
    }
    // The user changes these in system settings, so re-check when coming back.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        notificationsGranted = ReminderPermissions.notificationsGranted(context)
        exactAlarmsGranted = ReminderPermissions.canScheduleExactAlarms(context)
    }

    var showNameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
        val current = preferences
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else {
            SettingsContent(
                preferences = current,
                notificationsGranted = notificationsGranted,
                exactAlarmsGranted = exactAlarmsGranted,
                contentPadding = innerPadding,
                onNameClick = { showNameDialog = true },
                onThemeModeSelected = viewModel::onThemeModeSelected,
                onDynamicColorChanged = viewModel::onDynamicColorChanged,
                onLanguageClick = { openAppLanguageSettings(context) },
                onNotificationsClick = { openNotificationSettings(context) },
                onExactAlarmsClick = { openExactAlarmSettings(context) },
                onDeleteClick = { showDeleteDialog = true }
            )
        }
    }

    val currentPreferences = preferences
    if (showNameDialog && currentPreferences != null) {
        NameDialog(
            initialName = currentPreferences.userName,
            onConfirm = {
                viewModel.onNameChanged(it)
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.settings_delete_dialog_title)) },
            text = { Text(stringResource(R.string.settings_delete_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onDeleteAllData()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.settings_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsContent(
    preferences: UserPreferences,
    notificationsGranted: Boolean,
    exactAlarmsGranted: Boolean,
    contentPadding: PaddingValues,
    onNameClick: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onLanguageClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onExactAlarmsClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val showDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val showLanguage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val languageName = remember { currentLanguageName() }
    val versionName = remember { context.appVersionName() }
    val statusAllowed = stringResource(R.string.settings_status_allowed)
    val statusOff = stringResource(R.string.settings_status_off)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
    ) {
        item(key = "profile-header") { SectionHeader(stringResource(R.string.settings_section_profile)) }
        item(key = "profile-name") {
            SettingsItem(
                icon = Icons.Filled.Person,
                title = stringResource(R.string.settings_name_title),
                summary = preferences.userName.ifBlank { stringResource(R.string.settings_name_not_set) },
                index = 0,
                count = 1,
                onClick = onNameClick
            )
        }

        item(key = "appearance-header") { SectionHeader(stringResource(R.string.settings_section_appearance)) }
        item(key = "theme-mode") {
            ThemeModeSelector(
                selected = preferences.themeMode,
                onSelected = onThemeModeSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        if (showDynamicColor) {
            item(key = "dynamic-color") {
                SettingsSwitchItem(
                    icon = Icons.Filled.Palette,
                    title = stringResource(R.string.settings_dynamic_color_title),
                    summary = stringResource(R.string.settings_dynamic_color_summary),
                    checked = preferences.useDynamicColor,
                    onCheckedChange = onDynamicColorChanged,
                    index = 0,
                    count = 1
                )
            }
        }

        if (showLanguage) {
            item(key = "language-header") { SectionHeader(stringResource(R.string.settings_section_language)) }
            item(key = "language") {
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.settings_language_title),
                    summary = stringResource(R.string.settings_language_summary_format, languageName),
                    index = 0,
                    count = 1,
                    onClick = onLanguageClick
                )
            }
        }

        item(key = "reminders-header") { SectionHeader(stringResource(R.string.settings_section_reminders)) }
        item(key = "reminders-notifications") {
            SettingsItem(
                icon = Icons.Filled.Notifications,
                title = stringResource(R.string.settings_notifications_title),
                summary = if (notificationsGranted) statusAllowed else statusOff,
                index = 0,
                count = 2,
                onClick = onNotificationsClick
            )
        }
        item(key = "reminders-exact-alarms") {
            SettingsItem(
                icon = Icons.Filled.Alarm,
                title = stringResource(R.string.settings_exact_alarms_title),
                summary = if (exactAlarmsGranted) statusAllowed else statusOff,
                index = 1,
                count = 2,
                onClick = onExactAlarmsClick
            )
        }

        item(key = "privacy-header") { SectionHeader(stringResource(R.string.settings_section_privacy)) }
        item(key = "privacy-delete") {
            SettingsItem(
                icon = Icons.Filled.DeleteForever,
                title = stringResource(R.string.settings_delete_title),
                summary = stringResource(R.string.settings_delete_summary),
                index = 0,
                count = 1,
                isDestructive = true,
                onClick = onDeleteClick
            )
        }

        item(key = "about-header") { SectionHeader(stringResource(R.string.settings_section_about)) }
        item(key = "about-version") {
            SettingsItem(
                icon = Icons.Filled.Info,
                title = stringResource(R.string.settings_version_title),
                summary = versionName.ifBlank { "-" },
                index = 0,
                count = 2
            )
        }
        item(key = "about-disclaimer") {
            SettingsItem(
                icon = Icons.Filled.Warning,
                title = stringResource(R.string.settings_about_title),
                summary = stringResource(R.string.settings_about_message),
                index = 1,
                count = 2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    summary: String,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shapes = ListItemDefaults.segmentedShapes(index = index, count = count)
    val colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val accent = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val itemModifier = modifier.padding(horizontal = 16.dp)

    if (onClick != null) {
        SegmentedListItem(
            onClick = onClick,
            shapes = shapes,
            modifier = itemModifier,
            colors = colors,
            leadingContent = { Icon(icon, contentDescription = null, tint = accent) },
            supportingContent = { Text(summary) },
            content = { Text(title, color = accent) }
        )
    } else {
        SegmentedListItem(
            shapes = shapes,
            modifier = itemModifier,
            colors = colors,
            leadingContent = { Icon(icon, contentDescription = null, tint = accent) },
            supportingContent = { Text(summary) },
            content = { Text(title, color = accent) }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier
) {
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier.padding(horizontal = 16.dp),
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        supportingContent = { Text(summary) },
        content = { Text(title) }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Strings are resolved here: the ButtonGroup content lambda is not composable.
    val labels = ThemeMode.entries.associateWith { mode ->
        stringResource(
            when (mode) {
                ThemeMode.SYSTEM -> R.string.settings_theme_system
                ThemeMode.LIGHT -> R.string.settings_theme_light
                ThemeMode.DARK -> R.string.settings_theme_dark
            }
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.settings_theme_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        ButtonGroup(
            overflowIndicator = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeMode.entries.forEach { mode ->
                toggleableItem(
                    checked = mode == selected,
                    label = labels.getValue(mode),
                    onCheckedChange = { checked -> if (checked) onSelected(mode) },
                    weight = 1f
                )
            }
        }
    }
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

@Composable
private fun NameDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_name_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(MAX_NAME_LENGTH) },
                label = { Text(stringResource(R.string.onboarding_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

private fun currentLanguageName(): String {
    val locale = Locale.getDefault()
    return locale.getDisplayLanguage(locale).replaceFirstChar { char -> char.titlecase(locale) }
}

@Suppress("DEPRECATION")
private fun Context.appVersionName(): String = try {
    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }
    info.versionName.orEmpty()
} catch (e: PackageManager.NameNotFoundException) {
    ""
}

private fun openAppLanguageSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val intent = Intent(
            Settings.ACTION_APP_LOCALE_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // No screen to handle it on this device: nothing to do.
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