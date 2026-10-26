package com.gcoded.meditrack.reminder

import com.gcoded.meditrack.di.ApplicationScope
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.repository.MedicationRepository
import com.gcoded.meditrack.domain.usecase.ObserveTodayUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watches the database while the app process is alive and keeps the alarm and the
 * notifications in sync: any medication change or dose status change re-arms the alarm,
 * and a dose logged in the app dismisses its notification.
 */
@Singleton
class ReminderSyncer @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val medicationRepository: MedicationRepository,
    private val observeToday: ObserveTodayUseCase,
    private val scheduler: ReminderScheduler,
    private val notifier: DoseNotifier
) {

    private var started = false

    @OptIn(FlowPreview::class)
    fun start() {
        if (started) return
        started = true

        scope.launch {
            merge(
                medicationRepository.observeMedications().map { },
                observeToday()
                    .distinctUntilChangedBy { summary -> summary.slots.map { it.key to it.status } }
                    .map { }
            )
                .debounce(300)
                .collect { scheduler.reschedule() }
        }

        scope.launch {
            observeToday().collect { summary ->
                summary.slots
                    .filter { it.status == DoseSlotStatus.TAKEN || it.status == DoseSlotStatus.SKIPPED }
                    .forEach { notifier.cancel(it) }
            }
        }
    }
}
