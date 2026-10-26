package com.gcoded.meditrack.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector
import com.gcoded.meditrack.R

enum class TopLevelDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    TODAY(
        route = MediTrackDestination.Today.route,
        labelRes = R.string.nav_today_label,
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today
    ),
    HISTORY(
        route = MediTrackDestination.History.route,
        labelRes = R.string.nav_history_label,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    MEDICATIONS(
        route = MediTrackDestination.Medications.route,
        labelRes = R.string.nav_medications_label,
        selectedIcon = Icons.Filled.Medication,
        unselectedIcon = Icons.Outlined.Medication
    )
}
