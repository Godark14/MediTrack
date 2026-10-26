package com.gcoded.meditrack.debug

import com.gcoded.meditrack.domain.model.DoseSchedule
import com.gcoded.meditrack.domain.model.DoseStatus
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationForm
import com.gcoded.meditrack.domain.model.MedicationWithSchedules
import com.gcoded.meditrack.domain.repository.DoseLogRepository
import com.gcoded.meditrack.domain.repository.MedicationRepository
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlin.random.Random

/** Generates 3 medications and 30 days of realistic history (about 82% taken). */
class SampleDataSeeder @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val doseLogRepository: DoseLogRepository,
    private val clock: Clock
) {

    suspend fun seed() {
        val today = LocalDate.now(clock)
        val random = Random(42)
        val samples = listOf(
            Triple("Metformin", "500 mg", listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))),
            Triple("Vitamin D", "1000 IU", listOf(LocalTime.of(9, 0))),
            Triple("Lisinopril", "10 mg", listOf(LocalTime.of(7, 30)))
        )

        samples.forEach { (name, dosage, times) ->
            val medicationId = medicationRepository.saveMedication(
                MedicationWithSchedules(
                    medication = Medication(
                        name = name,
                        dosage = dosage,
                        form = MedicationForm.PILL,
                        notes = "",
                        startDate = today.minusDays(45)
                    ),
                    schedules = times.map { DoseSchedule(time = it) }
                )
            )
            // Past days only: today is left untouched so reminders and the Today screen stay real.
            for (offset in 1..30) {
                val date = today.minusDays(offset.toLong())
                times.forEach { time ->
                    val roll = random.nextInt(100)
                    when {
                        roll < 82 -> doseLogRepository.logDose(medicationId, date, time, DoseStatus.TAKEN)
                        roll < 90 -> doseLogRepository.logDose(medicationId, date, time, DoseStatus.SKIPPED)
                        else -> Unit // left unlogged: counts as missed
                    }
                }
            }
        }
    }
}
