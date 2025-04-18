package me.timeto.shared.ui.history.form

import me.timeto.shared.UnixTime

object HistoryFormUtils {

    fun prepTimeNote(
        time: Int,
    ): String {
        val today: Int = UnixTime().localDay
        val unixTime = UnixTime(time)
        return if (today == unixTime.localDay)
            "Today " + unixTime.getStringByComponents(
                listOf(
                    UnixTime.StringComponent.hhmm24,
                )
            )
        else unixTime.getStringByComponents(
            listOf(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
                UnixTime.StringComponent.comma,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.dayOfWeek3,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.hhmm24,
            )
        )
    }
}
