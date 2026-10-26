package com.gcoded.meditrack.domain.model

data class MedicationAdherence(
    val medication: Medication,
    val taken: Int,
    val resolved: Int
) {
    val rate: Float get() = if (resolved == 0) 0f else taken.toFloat() / resolved
}

data class AdherenceReport(
    val days: List<DayAdherence>,
    val takenCount: Int,
    val resolvedCount: Int,
    val perMedication: List<MedicationAdherence>,
    val currentStreakDays: Int
) {
    /** Null when no dose has been resolved yet, so the UI can show "no data" instead of 0%. */
    val rate: Float? get() = if (resolvedCount == 0) null else takenCount.toFloat() / resolvedCount
}
