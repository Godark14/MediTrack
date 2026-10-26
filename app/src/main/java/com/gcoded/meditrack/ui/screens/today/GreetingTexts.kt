package com.gcoded.meditrack.ui.screens.today

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.gcoded.meditrack.R
import com.gcoded.meditrack.domain.model.DayPeriod
import com.gcoded.meditrack.domain.model.Greeting
import com.gcoded.meditrack.domain.model.TodayMessage
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** "Bonsoir, Godwin" (or just "Bonsoir" when no name was given). */
@Composable
fun Greeting.titleText(): String {
    val salutation = stringResource(
        when (period) {
            DayPeriod.MORNING -> R.string.greeting_morning
            DayPeriod.AFTERNOON -> R.string.greeting_afternoon
            DayPeriod.EVENING -> R.string.greeting_evening
        }
    )
    return if (userName.isBlank()) {
        salutation
    } else {
        stringResource(R.string.greeting_with_name_format, salutation, userName)
    }
}

@Composable
fun TodayMessage.text(): String = when (this) {
    TodayMessage.NoDosesToday -> stringResource(R.string.message_no_doses)
    is TodayMessage.AllDone -> stringResource(
        if (isLate) R.string.message_all_done_late else R.string.message_all_done
    )
    TodayMessage.AllSkipped -> stringResource(R.string.message_all_skipped)
    TodayMessage.CheckIn -> stringResource(R.string.message_check_in)
    is TodayMessage.DueNow -> pluralStringResource(R.plurals.message_due_now, count, count)
    is TodayMessage.Missed -> pluralStringResource(R.plurals.message_missed, count, count)
    is TodayMessage.NextDose -> stringResource(
        R.string.message_next_dose_format,
        time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
        medicationName
    )
}