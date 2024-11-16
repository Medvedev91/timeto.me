package me.timeto.shared.models

import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb

class DayIntervalsUi(
    val unixDay: Int,
    val intervalsUi: List<IntervalUi>,
    dayStringFormat: DAY_STRING_FORMAT,
) {

    val dayString: String =
        if ((dayStringFormat == DAY_STRING_FORMAT.ALL) || ((unixDay % 2) == 0))
            "${UnixTime.byLocalDay(unixDay).dayOfMonth()}"
        else ""

    class IntervalUi(
        val intervalDb: IntervalDb?,
        val timeStart: Int,
        val seconds: Int,
    ) {
        val activityDb: ActivityDb? = intervalDb?.getActivityDbCached()
        val ratio: Float = seconds.toFloat() / 86_400
        val timeFinish: Int = timeStart + seconds
    }

    enum class DAY_STRING_FORMAT {
        ALL, EVEN,
    }

    ///

    companion object {

        suspend fun buildList(
            dayStart: Int,
            dayFinish: Int,
            utcOffset: Int,
        ): List<DayIntervalsUi> {

            val timeStart: Int = UnixTime.byLocalDay(dayStart, utcOffset).time
            val timeFinish: Int = UnixTime.byLocalDay(dayFinish + 1, utcOffset).time - 1

            //
            // Preparing the intervals list

            val intervalsAsc: MutableList<IntervalDb> = IntervalDb
                .getBetweenIdDesc(timeStart, timeFinish)
                .reversed()
                .toMutableList()

            // Previous interval
            IntervalDb.getBetweenIdDesc(0, timeStart - 1, 1).firstOrNull()?.let { prevInterval ->
                intervalsAsc.add(0, prevInterval) // 0 idx - to start
            }

            ////

            val now = time()
            val barDayFormat = if (dayStart == dayFinish) DAY_STRING_FORMAT.ALL else DAY_STRING_FORMAT.EVEN
            val daysIntervalsUI: List<DayIntervalsUi> = (dayStart..dayFinish).map { day ->
                val dayTimeStart: Int = UnixTime.byLocalDay(day, utcOffset).time
                val dayTimeFinish: Int = dayTimeStart + 86_400
                val dayMaxTimeFinish: Int = dayTimeFinish.limitMax(now)

                if ((now <= dayTimeStart) ||
                    intervalsAsc.isEmpty() ||
                    (dayTimeFinish <= intervalsAsc.first().id)
                ) {
                    return@map DayIntervalsUi(
                        unixDay = day,
                        intervalsUi = listOf(IntervalUi(null, dayTimeStart, 86_400)),
                        dayStringFormat = barDayFormat,
                    )
                }

                val firstInterval: IntervalDb = intervalsAsc.first()

                val daySections = mutableListOf<IntervalUi>()
                val dayIntervals = intervalsAsc.filter { it.id >= dayTimeStart && it.id < dayTimeFinish }

                // Adding leading section. Relevant for the beginning of history.
                if (firstInterval.id > dayTimeStart)
                    daySections.add(IntervalUi(null, dayTimeStart, firstInterval.id - dayTimeStart))
                else {
                    val todayFirstIntervalOrNull: IntervalDb? = dayIntervals.firstOrNull()
                    if ((todayFirstIntervalOrNull == null) || (todayFirstIntervalOrNull.id > dayTimeStart)) {
                        val prevInterval = intervalsAsc.last { it.id < dayTimeStart }
                        val seconds = (todayFirstIntervalOrNull?.id ?: dayMaxTimeFinish) - dayTimeStart
                        daySections.add(IntervalUi(prevInterval, dayTimeStart, seconds))
                    }
                }

                // Adding other sections
                dayIntervals.forEachIndexed { idx, interval ->
                    val nextIntervalTime =
                        if ((idx + 1) == dayIntervals.size) dayMaxTimeFinish
                        else dayIntervals[idx + 1].id
                    val seconds = nextIntervalTime - interval.id
                    daySections.add(IntervalUi(interval, interval.id, seconds))
                }

                // For today
                val trailingPadding = dayTimeFinish - dayMaxTimeFinish
                if (trailingPadding > 0)
                    daySections.add(IntervalUi(null, dayMaxTimeFinish, trailingPadding))

                DayIntervalsUi(day, daySections, barDayFormat)
            }

            return daysIntervalsUI
        }
    }
}
