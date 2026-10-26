package com.gcoded.meditrack.domain.model

data class MedicationWithSchedules(
    val medication: Medication,
    val schedules: List<DoseSchedule>
)
