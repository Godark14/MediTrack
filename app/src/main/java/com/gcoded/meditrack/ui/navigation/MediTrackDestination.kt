package com.gcoded.meditrack.ui.navigation

sealed class MediTrackDestination(val route: String) {
    data object Today : MediTrackDestination("today")
    data object History : MediTrackDestination("history")
    data object Medications : MediTrackDestination("medications")
    data object Settings : MediTrackDestination("settings")

    data object AddEditMedication : MediTrackDestination("add_edit_medication") {
        const val ARG_MEDICATION_ID = "medicationId"
        const val NO_MEDICATION_ID = -1L
        val routeWithArg = "add_edit_medication?$ARG_MEDICATION_ID={$ARG_MEDICATION_ID}"

        fun createRoute(medicationId: Long? = null): String =
            if (medicationId != null) {
                "add_edit_medication?$ARG_MEDICATION_ID=$medicationId"
            } else {
                "add_edit_medication"
            }
    }
}