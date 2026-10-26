package com.gcoded.meditrack.domain.model

/** Doses of the period that were missed or deliberately skipped, in chronological order. */
val AdherenceReport.incidents: List<DoseSlot>
    get() = days.flatMap { day ->
        day.slots.filter { it.status == DoseSlotStatus.MISSED || it.status == DoseSlotStatus.SKIPPED }
    }
