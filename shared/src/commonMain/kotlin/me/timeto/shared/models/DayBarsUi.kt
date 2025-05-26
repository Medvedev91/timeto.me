package me.timeto.shared.models

import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.misc.time

class DayBarsUi(
    val unixDay: Int,
    val barsUi: List<BarUi>,
    dayStringFormat: DAY_STRING_FORMAT,
) {

    val dayString: String =
        if ((dayStringFormat == DAY_STRING_FORMAT.ALL) || ((unixDay % 2) == 0))
            "${UnixTime.byLocalDay(unixDay).dayOfMonth()}"
        else ""

    class BarUi(
        val intervalDb: IntervalDb?,
        val timeStart: Int,
        val seconds: Int,
    ) {
        val intervalTf: TextFeatures = (intervalDb?.note ?: "").textFeatures()
        val activityDb: ActivityDb? = intervalDb?.selectActivityDbCached()
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
        ): List<DayBarsUi> {

            val timeStart: Int = UnixTime.byLocalDay(dayStart, utcOffset).time
            val timeFinish: Int = UnixTime.byLocalDay(dayFinish + 1, utcOffset).time - 1

            //
            // Preparing the intervals list

            val intervalsAsc: MutableList<IntervalDb> = IntervalDb
                .selectBetweenIdDesc(timeStart, timeFinish)
                .reversed()
                .toMutableList()

            // Previous interval
            IntervalDb.selectBetweenIdDesc(0, timeStart - 1, 1).firstOrNull()?.let { prevIntervalDb ->
                intervalsAsc.add(0, prevIntervalDb) // 0 idx - to start
            }

            ///

            val now = time()
            val barDayFormat: DAY_STRING_FORMAT =
                if (dayStart == dayFinish) DAY_STRING_FORMAT.ALL else DAY_STRING_FORMAT.EVEN
            val daysBarsUi: List<DayBarsUi> = (dayStart..dayFinish).map { dayBarUi ->
                val dayTimeStart: Int = UnixTime.byLocalDay(dayBarUi, utcOffset).time
                val dayTimeFinish: Int = dayTimeStart + 86_400
                val dayMaxTimeFinish: Int = dayTimeFinish.limitMax(now)

                if ((now <= dayTimeStart) ||
                    intervalsAsc.isEmpty() ||
                    (dayTimeFinish <= intervalsAsc.first().id)
                ) {
                    return@map DayBarsUi(
                        unixDay = dayBarUi,
                        barsUi = listOf(BarUi(null, dayTimeStart, 86_400)),
                        dayStringFormat = barDayFormat,
                    )
                }

                val firstInterval: IntervalDb = intervalsAsc.first()

                val dayBarsUi = mutableListOf<BarUi>()
                val dayIntervalsDb: List<IntervalDb> =
                    intervalsAsc.filter { it.id >= dayTimeStart && it.id < dayTimeFinish }

                // Adding leading section. Relevant for the beginning of history.
                if (firstInterval.id > dayTimeStart)
                    dayBarsUi.add(BarUi(null, dayTimeStart, firstInterval.id - dayTimeStart))
                else {
                    val todayFirstIntervalOrNull: IntervalDb? = dayIntervalsDb.firstOrNull()
                    if ((todayFirstIntervalOrNull == null) || (todayFirstIntervalOrNull.id > dayTimeStart)) {
                        val prevInterval = intervalsAsc.last { it.id < dayTimeStart }
                        val seconds = (todayFirstIntervalOrNull?.id ?: dayMaxTimeFinish) - dayTimeStart
                        dayBarsUi.add(BarUi(prevInterval, dayTimeStart, seconds))
                    }
                }

                // Adding other sections
                dayIntervalsDb.forEachIndexed { idx, intervalDb ->
                    val nextIntervalTime =
                        if ((idx + 1) == dayIntervalsDb.size) dayMaxTimeFinish
                        else dayIntervalsDb[idx + 1].id
                    val seconds = nextIntervalTime - intervalDb.id
                    dayBarsUi.add(BarUi(intervalDb, intervalDb.id, seconds))
                }

                // For today
                val trailingPadding: Int = dayTimeFinish - dayMaxTimeFinish
                if (trailingPadding > 0)
                    dayBarsUi.add(BarUi(null, dayMaxTimeFinish, trailingPadding))

                DayBarsUi(dayBarUi, dayBarsUi, barDayFormat)
            }

            return daysBarsUi
        }
    }
}
