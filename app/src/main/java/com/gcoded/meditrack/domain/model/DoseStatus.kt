package com.gcoded.meditrack.domain.model

enum class DoseStatus {
    TAKEN, SKIPPED;

    companion object {
        fun fromName(name: String): DoseStatus =
            entries.firstOrNull { it.name == name } ?: SKIPPED
    }
}
