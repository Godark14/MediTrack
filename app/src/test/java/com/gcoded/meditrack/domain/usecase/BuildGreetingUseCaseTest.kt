package com.gcoded.meditrack.domain.usecase

import com.gcoded.meditrack.domain.model.DayPeriod
import com.gcoded.meditrack.domain.model.DaySummary
import com.gcoded.meditrack.domain.model.DoseSlot
import com.gcoded.meditrack.domain.model.DoseSlotStatus
import com.gcoded.meditrack.domain.model.Medication
import com.gcoded.meditrack.domain.model.MedicationForm
import com.gcoded.meditrack.domain.model.TodayMessage
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class BuildGreetingUseCaseTest {

    private val useCase = BuildGreetingUseCase()
    private val date = LocalDate.of(2026, 10, 25)

    private fun slot(
        hour: Int,
        status: DoseSlotStatus,
        name: String = "Aspirin"
    ) = DoseSlot(
        medication = Medication(
            id = 1,
            name = name,
            dosage = "100 mg",
            form = MedicationForm.PILL,
            notes = "",
            startDate = date.minusDays(10)
        ),
        date = date,
        time = LocalTime.of(hour, 0),
        status = status
    )

    private fun message(vararg slots: DoseSlot, now: LocalDateTime = date.atTime(12, 0)): TodayMessage =
        useCase(DaySummary(date, slots.toList()), now, "Godwin").message

    @Test
    fun salutationFollowsTheTimeOfDay() {
        assertEquals(DayPeriod.EVENING, DayPeriod.at(LocalTime.of(4, 59)))
        assertEquals(DayPeriod.MORNING, DayPeriod.at(LocalTime.of(5, 0)))
        assertEquals(DayPeriod.MORNING, DayPeriod.at(LocalTime.of(11, 59)))
        assertEquals(DayPeriod.AFTERNOON, DayPeriod.at(LocalTime.of(12, 0)))
        assertEquals(DayPeriod.AFTERNOON, DayPeriod.at(LocalTime.of(17, 59)))
        assertEquals(DayPeriod.EVENING, DayPeriod.at(LocalTime.of(18, 0)))
        assertEquals(DayPeriod.EVENING, DayPeriod.at(LocalTime.of(23, 30)))
    }

    @Test
    fun theUserNameIsTrimmed() {
        val greeting = useCase(DaySummary(date, emptyList()), date.atTime(9, 0), "  Godwin  ")
        assertEquals("Godwin", greeting.userName)
        assertEquals(DayPeriod.MORNING, greeting.period)
    }

    @Test
    fun noSlotsMeansNothingScheduled() {
        assertEquals(TodayMessage.NoDosesToday, message())
    }

    @Test
    fun everythingTakenIsAllDone() {
        val result = message(slot(8, DoseSlotStatus.TAKEN), slot(20, DoseSlotStatus.TAKEN))
        assertEquals(TodayMessage.AllDone(isLate = false), result)
    }

    @Test
    fun allDoneLateInTheEveningWishesGoodNight() {
        val result = message(
            slot(8, DoseSlotStatus.TAKEN),
            now = date.atTime(21, 30)
        )
        assertEquals(TodayMessage.AllDone(isLate = true), result)
    }

    @Test
    fun everythingSkippedIsReportedAsSuch() {
        assertEquals(TodayMessage.AllSkipped, message(slot(8, DoseSlotStatus.SKIPPED)))
    }

    @Test
    fun nothingLoggedYetAndADoseIsDueAsksTheCheckInQuestion() {
        val result = message(slot(8, DoseSlotStatus.DUE), slot(20, DoseSlotStatus.UPCOMING))
        assertEquals(TodayMessage.CheckIn, result)
    }

    @Test
    fun nothingLoggedYetAndADoseWasMissedAsksTheCheckInQuestion() {
        val result = message(slot(8, DoseSlotStatus.MISSED), slot(20, DoseSlotStatus.UPCOMING))
        assertEquals(TodayMessage.CheckIn, result)
    }

    @Test
    fun onceSomethingIsTakenDueDosesAreCounted() {
        val result = message(
            slot(8, DoseSlotStatus.TAKEN),
            slot(12, DoseSlotStatus.DUE),
            slot(12, DoseSlotStatus.DUE, name = "Zinc")
        )
        assertEquals(TodayMessage.DueNow(2), result)
    }

    @Test
    fun missedDosesAreCountedWhenNothingIsDue() {
        val result = message(
            slot(8, DoseSlotStatus.TAKEN),
            slot(10, DoseSlotStatus.MISSED),
            slot(20, DoseSlotStatus.UPCOMING)
        )
        assertEquals(TodayMessage.Missed(1), result)
    }

    @Test
    fun dueTakesPriorityOverMissed() {
        val result = message(
            slot(6, DoseSlotStatus.TAKEN),
            slot(8, DoseSlotStatus.MISSED),
            slot(12, DoseSlotStatus.DUE)
        )
        assertEquals(TodayMessage.DueNow(1), result)
    }

    @Test
    fun onlyUpcomingDosesLeftShowsTheNextOne() {
        val result = message(
            slot(8, DoseSlotStatus.TAKEN),
            slot(20, DoseSlotStatus.UPCOMING, name = "Zinc"),
            slot(14, DoseSlotStatus.UPCOMING, name = "Magnesium")
        )
        assertEquals(TodayMessage.NextDose(LocalTime.of(14, 0), "Magnesium"), result)
    }

    @Test
    fun aMorningWithOnlyUpcomingDosesShowsTheNextOne() {
        val result = message(
            slot(8, DoseSlotStatus.UPCOMING),
            now = date.atTime(6, 30)
        )
        assertEquals(TodayMessage.NextDose(LocalTime.of(8, 0), "Aspirin"), result)
    }
}