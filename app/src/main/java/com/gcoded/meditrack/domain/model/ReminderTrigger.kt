package com.gcoded.meditrack.domain.model

import java.time.LocalDateTime

/** The next moment the user must be reminded, with every dose slot due at that moment. */
data class ReminderTrigger(
    val triggerAt: LocalDateTime,
    val slots: List<DoseSlot>
)
