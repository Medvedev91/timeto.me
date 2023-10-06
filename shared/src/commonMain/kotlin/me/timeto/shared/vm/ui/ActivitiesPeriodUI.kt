package me.timeto.shared.vm.ui

import me.timeto.shared.*
import me.timeto.shared.db.ActivityModel
import me.timeto.shared.db.IntervalModel

class ActivitiesPeriodUI(
    private val dayStart: Int,
    private val dayFinish: Int,
    private val lastInterval: IntervalModel,
    // TRICK Without the last interval, use calcDuration()
    private val mapActivitySeconds: Map<Int, Int>,
) {

    fun getActivitiesUI(): List<ActivityUI> {
        val daysCount = dayFinish - dayStart + 1
        val totalSeconds = daysCount * 86_400
        val activityIds: Set<Int> = mapActivitySeconds.keys + lastInterval.activity_id
        return activityIds
            .map { activityId ->
                val activity = DI.getActivityByIdOrNull(activityId)!!
                val duration = calcDuration(activity)
                ActivityUI(
                    activity = activity,
                    seconds = duration,
                    ratio = duration.toFloat() / totalSeconds,
                    secondsPerDay = duration / daysCount,
                )
            }
            .sortedByDescending { it.seconds }
    }

    fun calcDuration(activity: ActivityModel): Int {
        var duration = mapActivitySeconds[activity.id] ?: 0
        if (activity.id == lastInterval.activity_id)
            duration += (time() - lastInterval.id)
        return duration
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

    ///

    companion object {

        suspend fun build(
            dayStart: Int,
            dayFinish: Int,
            utcOffset: Int,
        ): ActivitiesPeriodUI {

            val timeStart: Int = UnixTime.byLocalDay(dayStart, utcOffset).time
            val timeFinish: Int = UnixTime.byLocalDay(dayFinish + 1, utcOffset).time - 1

            val intervalsAsc: MutableList<IntervalModel> = IntervalModel
                .getBetweenIdDesc(timeStart, timeFinish)
                .reversed()
                .toMutableList()

            // Previous interval
            IntervalModel
                .getBetweenIdDesc(0, timeStart - 1, 1)
                .firstOrNull()
                ?.let { prevInterval ->
                    // 0 idx - to start
                    intervalsAsc.add(0, prevInterval)
                }

            val mapActivitySeconds = mutableMapOf<Int, Int>()
            intervalsAsc.forEachIndexed { idx, interval ->
                // Last interval
                if ((idx + 1) == intervalsAsc.size)
                    return@forEachIndexed
                val nextInterval = intervalsAsc[idx + 1]
                val duration = nextInterval.id - interval.id.limitMin(timeStart)
                mapActivitySeconds.incOrSet(interval.activity_id, duration)
            }

            return ActivitiesPeriodUI(
                dayStart = dayStart,
                dayFinish = dayFinish,
                lastInterval = intervalsAsc.last(),
                mapActivitySeconds = mapActivitySeconds,
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
