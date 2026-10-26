package com.gcoded.meditrack.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gcoded.meditrack.ui.screens.history.HistoryScreen
import com.gcoded.meditrack.ui.screens.medication.AddEditMedicationScreen
import com.gcoded.meditrack.ui.screens.medications.MedicationsScreen
import com.gcoded.meditrack.ui.screens.settings.SettingsScreen
import com.gcoded.meditrack.ui.screens.today.TodayScreen

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediTrackNavHost(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = TopLevelDestination.entries.any { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                ShortNavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == destination.route } == true
                        ShortNavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon
                                    else destination.unselectedIcon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MediTrackDestination.Today.route,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(MediTrackDestination.Today.route) {
                TodayScreen(
                    onAddMedicationClick = {
                        navController.navigate(MediTrackDestination.AddEditMedication.createRoute())
                    },
                    onSettingsClick = {
                        navController.navigate(MediTrackDestination.Settings.route)
                    }
                )
            }
            composable(MediTrackDestination.History.route) {
                HistoryScreen()
            }
            composable(MediTrackDestination.Medications.route) {
                MedicationsScreen(
                    onAddClick = {
                        navController.navigate(MediTrackDestination.AddEditMedication.createRoute())
                    },
                    onEditClick = { medicationId ->
                        navController.navigate(
                            MediTrackDestination.AddEditMedication.createRoute(medicationId)
                        )
                    }
                )
            }
            composable(
                route = MediTrackDestination.AddEditMedication.routeWithArg,
                arguments = listOf(
                    navArgument(MediTrackDestination.AddEditMedication.ARG_MEDICATION_ID) {
                        type = NavType.LongType
                        defaultValue = MediTrackDestination.AddEditMedication.NO_MEDICATION_ID
                    }
                )
            ) {
                AddEditMedicationScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(MediTrackDestination.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}