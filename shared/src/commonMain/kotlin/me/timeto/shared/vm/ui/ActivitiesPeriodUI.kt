package me.timeto.shared.vm.ui

import me.timeto.shared.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel

class ActivitiesPeriodUI(
    val barsUI: List<BarUI>,
) {

    class BarUI(
        val unixDay: Int,
        val sections: List<SectionItem>,
        dayStringFormat: DAY_STRING_FORMAT,
    ) {

        val dayString: String =
            if ((dayStringFormat == DAY_STRING_FORMAT.ALL) || ((unixDay % 2) == 0))
                "${UnixTime.byLocalDay(unixDay).dayOfMonth()}"
            else ""

        class SectionItem(
            val activity: ActivityModel?,
            val timeStart: Int,
            val seconds: Int,
        ) {
            val ratio: Float = seconds.toFloat() / 86_400
        }

        enum class DAY_STRING_FORMAT {
            ALL, EVEN,
        }
    }

    ///

    companion object {

        suspend fun buildList(
            dayStart: Int,
            dayFinish: Int,
            utcOffset: Int,
        ): ActivitiesPeriodUI {

            val timeStart: Int = UnixTime.byLocalDay(dayStart, utcOffset).time
            val timeFinish: Int = UnixTime.byLocalDay(dayFinish + 1, utcOffset).time - 1

            //
            // Preparing the intervals list

            val intervalsAsc: MutableList<IntervalModel> = IntervalModel
                .getBetweenIdDesc(timeStart, timeFinish)
                .reversed()
                .toMutableList()

            // Previous interval
            IntervalModel.getBetweenIdDesc(0, timeStart - 1, 1).firstOrNull()?.let { prevInterval ->
                intervalsAsc.add(0, prevInterval) // 0 idx - to start
            }

            ////

            val now = time()
            val barDayFormat = if (dayStart == dayFinish) BarUI.DAY_STRING_FORMAT.ALL else BarUI.DAY_STRING_FORMAT.EVEN
            val barsUI: List<BarUI> = (dayStart..dayFinish).map { day ->
                val dayTimeStart: Int = UnixTime.byLocalDay(day, utcOffset).time
                val dayTimeFinish: Int = dayTimeStart + 86_400
                val dayMaxTimeFinish: Int = dayTimeFinish.limitMax(now)

                if ((now <= dayTimeStart) ||
                    intervalsAsc.isEmpty() ||
                    (dayTimeFinish <= intervalsAsc.first().id)
                )
                    return@map BarUI(day, listOf(BarUI.SectionItem(null, dayTimeStart, 86_400)), barDayFormat)

                val firstInterval: IntervalModel = intervalsAsc.first()

                val daySections = mutableListOf<BarUI.SectionItem>()
                val dayIntervals = intervalsAsc.filter { it.id >= dayTimeStart && it.id < dayTimeFinish }

                // Adding leading section
                if (firstInterval.id >= dayTimeStart)
                    daySections.add(BarUI.SectionItem(null, dayTimeStart, firstInterval.id - dayTimeStart))
                else {
                    val prevInterval = intervalsAsc.last { it.id < dayTimeStart }
                    val seconds = (dayIntervals.firstOrNull()?.id ?: dayMaxTimeFinish) - dayTimeStart
                    daySections.add(BarUI.SectionItem(prevInterval.getActivityDI(), dayTimeStart, seconds))
                }

                // Adding other sections
                dayIntervals.forEachIndexed { idx, interval ->
                    val nextIntervalTime =
                        if ((idx + 1) == dayIntervals.size) dayMaxTimeFinish
                        else dayIntervals[idx + 1].id
                    val seconds = nextIntervalTime - interval.id
                    daySections.add(BarUI.SectionItem(interval.getActivityDI(), interval.id, seconds))
                }

                // For today
                val trailingPadding = dayTimeFinish - dayMaxTimeFinish
                if (trailingPadding > 0)
                    daySections.add(BarUI.SectionItem(null, dayMaxTimeFinish, trailingPadding))

                BarUI(day, daySections, barDayFormat)
            }

            return ActivitiesPeriodUI(
                barsUI = barsUI,
            )
        }
    }
}
