package com.gcoded.meditrack.domain.model

import java.time.LocalTime

enum class DayPeriod {
    MORNING, AFTERNOON, EVENING;

    companion object {
        fun at(time: LocalTime): DayPeriod = when (time.hour) {
            in 5..11 -> MORNING
            in 12..17 -> AFTERNOON
            else -> EVENING
        }
    }
}

sealed interface TodayMessage {
    data object NoDosesToday : TodayMessage
    data class AllDone(val isLate: Boolean) : TodayMessage
    data object AllSkipped : TodayMessage
    data object CheckIn : TodayMessage
    data class DueNow(val count: Int) : TodayMessage
    data class Missed(val count: Int) : TodayMessage
    data class NextDose(val time: LocalTime, val medicationName: String) : TodayMessage
}

data class Greeting(
    val period: DayPeriod,
    val userName: String,
    val message: TodayMessage
)