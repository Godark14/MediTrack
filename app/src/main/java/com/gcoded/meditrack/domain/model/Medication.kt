package com.gcoded.meditrack.domain.model

import java.time.LocalDate

data class Medication(
    val id: Long = 0,
    val name: String,
    val dosage: String,
    val form: MedicationForm,
    val notes: String,
    val isActive: Boolean = true,
    val startDate: LocalDate,
    val endDate: LocalDate? = null
)
