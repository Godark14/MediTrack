package com.gcoded.meditrack.domain.model

enum class MedicationForm {
    PILL, CAPSULE, LIQUID, INJECTION, DROPS, OTHER;

    companion object {
        fun fromName(name: String): MedicationForm =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
