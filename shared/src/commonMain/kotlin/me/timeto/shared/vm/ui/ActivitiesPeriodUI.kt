package me.timeto.shared.vm.ui

import me.timeto.shared.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel

class ActivitiesPeriodUI(
    val barsUI: List<BarUI>,
) {

    fun getActivitiesUI(): List<ActivityUI> {
        val daysCount = barsUI.size
        val totalSeconds = daysCount * 86_400
        val mapActivitySeconds: MutableMap<Int, Int> = mutableMapOf()
        barsUI.forEach { barUI ->
            barUI.sections.forEach { sectionItem ->
                val activity = sectionItem.activity
                if (activity != null)
                    mapActivitySeconds.incOrSet(activity.id, sectionItem.seconds)
            }
        }
        return mapActivitySeconds
            .map { (activityId, seconds) ->
                val activity = DI.getActivityByIdOrNull(activityId)!!
                ActivityUI(
                    activity = activity,
                    seconds = seconds,
                    ratio = seconds.toFloat() / totalSeconds,
                    secondsPerDay = seconds / daysCount,
                )
            }
            .sortedByDescending { it.seconds }
    }

    ///

    class ActivityUI(
        val activity: ActivityModel,
        val seconds: Int,
        val ratio: Float,
        secondsPerDay: Int,
    ) {
        val title = activity.name.textFeatures().textUi()
        val percentageString = "${(ratio * 100).toInt()}%"
        val perDayString: String = prepTimeString(secondsPerDay) + " / day"
        val totalTimeString: String = prepTimeString(seconds)
    }

    class BarUI(
        val unixDay: Int,
        val sections: List<SectionItem>,
    ) {

        class SectionItem(
            val activity: ActivityModel?,
            val timeStart: Int,
            val seconds: Int,
        ) {
            val ratio: Float = seconds.toFloat() / 86_400
        }
    }

    ///

    companion object {

        suspend fun build(
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
            val barsUI: List<BarUI> = (dayStart..dayFinish).map { day ->
                val dayTimeStart: Int = UnixTime.byLocalDay(day, utcOffset).time
                val dayTimeFinish: Int = dayTimeStart + 86_400
                val dayMaxTimeFinish: Int = dayTimeFinish.limitMax(now)

                if ((now <= dayTimeStart) ||
                    intervalsAsc.isEmpty() ||
                    (dayTimeFinish <= intervalsAsc.first().id)
                )
                    return@map BarUI(day, listOf(BarUI.SectionItem(null, dayTimeStart, 86_400)))

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

                BarUI(day, daySections)
            }

            return ActivitiesPeriodUI(
                barsUI = barsUI,
            )
        }
    }
}

private fun prepTimeString(seconds: Int): String {
    val (h, m, _) = seconds.toHms(roundToNextMinute = true)
    val items = mutableListOf<String>()
    if (h > 0) items.add("${h}h")
    if (m > 0) items.add("${m}m")
    return items.joinToString(" ")
}
