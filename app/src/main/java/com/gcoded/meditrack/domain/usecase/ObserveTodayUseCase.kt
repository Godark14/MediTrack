package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.DaySummary
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import com.gcoded.meditrack.domain.repository.MedicationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Emits today's [DaySummary], refreshed whenever medications or logs change,
 * every minute (so DUE / MISSED statuses update on their own), and at midnight.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveTodayUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val buildDaySlots: BuildDaySlotsUseCase,
    private val clock: Clock
) {

    operator fun invoke(): Flow<DaySummary> =
        minuteTicker()
            .map { it.toLocalDate() }
            .distinctUntilChanged()
            .flatMapLatest { date ->
                combine(
                    medicationRepository.observeMedications(),
                    doseLogRepository.observeLogsBetween(date, date),
                    minuteTicker()
                ) { medications, logs, now ->
                    DaySummary(
                        date = date,
                        slots = buildDaySlots(date, medications, logs, now)
                    )
                }
            }

    private fun minuteTicker(): Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now(clock))
            delay(60_000L - (clock.millis() % 60_000L))
        }
    }
}
