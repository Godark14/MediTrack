package com.gcoded.meditrack.data.repository

import com.gcoded.meditrack.data.local.entity.DoseLogEntity
import com.gcoded.meditrack.data.local.entity.MedicationEntity
import com.gcoded.meditrack.data.local.entity.MedicationWithSchedulesRelation
import com.gcoded.meditrack.data.local.entity.ScheduleEntity
import com.gcoded.meditrack.domain.model.DoseLog
import com.gcoded.meditrack.domain.model.DoseSchedule
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationForm
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

private fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

private fun minuteOfDayToTime(minuteOfDay: Int): LocalTime =
    LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)

private fun Set<DayOfWeek>.toMask(): Int =
    fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

private fun maskToDays(mask: Int): Set<DayOfWeek> =
    DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet()

internal fun MedicationEntity.toDomain() = Medication(
    id = id,
    name = name,
    dosage = dosage,
    form = MedicationForm.fromName(form),
    notes = notes,
    isActive = isActive,
    startDate = LocalDate.ofEpochDay(startEpochDay),
    endDate = endEpochDay?.let { LocalDate.ofEpochDay(it) }
)

internal fun Medication.toEntity() = MedicationEntity(
    id = id,
    name = name,
    dosage = dosage,
    form = form.name,
    notes = notes,
    isActive = isActive,
    startEpochDay = startDate.toEpochDay(),
    endEpochDay = endDate?.toEpochDay()
)

internal fun ScheduleEntity.toDomain() = DoseSchedule(
    id = id,
    medicationId = medicationId,
    time = minuteOfDayToTime(minuteOfDay),
    daysOfWeek = maskToDays(daysMask)
)

internal fun DoseSchedule.toEntity() = ScheduleEntity(
    id = id,
    medicationId = medicationId,
    minuteOfDay = time.toMinuteOfDay(),
    daysMask = daysOfWeek.toMask()
)

internal fun MedicationWithSchedulesRelation.toDomain() = MedicationWithSchedules(
    medication = medication.toDomain(),
    schedules = schedules.map { it.toDomain() }.sortedBy { it.time }
)

internal fun DoseLogEntity.toDomain() = DoseLog(
    id = id,
    medicationId = medicationId,
    date = LocalDate.ofEpochDay(epochDay),
    time = minuteOfDayToTime(minuteOfDay),
    status = DoseStatus.fromName(status),
    loggedAt = Instant.ofEpochMilli(loggedAtEpochMillis)
)

internal fun DoseLog.toEntity() = DoseLogEntity(
    id = id,
    medicationId = medicationId,
    epochDay = date.toEpochDay(),
    minuteOfDay = time.toMinuteOfDay(),
    status = status.name,
    loggedAtEpochMillis = loggedAt.toEpochMilli()
)
