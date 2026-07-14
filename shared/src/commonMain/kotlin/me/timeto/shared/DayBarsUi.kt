package me.timeto.shared

import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import kotlin.math.absoluteValue

class DayBarsUi(
    val unixDay: Int,
    val barsUi: List<BarUi>,
    dayStringFormat: DAY_STRING_FORMAT,
) {

    val dayString: String =
        if ((dayStringFormat == DAY_STRING_FORMAT.ALL) || ((unixDay % 2) == 0))
            "${UnixTime.byLocalDay(unixDay).dayOfMonth()}"
        else ""

    fun buildActivityStats(
        activityDb: ActivityDb,
    ): ActivityStats {
        val recursiveActivitiesDb = ActivityDb.selectParentRecursiveMapCached()
        val activityBarsUi: List<BarUi> = barsUi
            .filter { barUi ->
                val barActivityId = barUi.intervalDb?.activityId
                if (barActivityId == null)
                    return@filter false
                val recursiveIds: List<Int> =
                    recursiveActivitiesDb[activityDb.id]!!.map { it.id } + activityDb.id
                barActivityId in recursiveIds
            }

        val intervalsSeconds: Int =
            activityBarsUi.sumOf { it.seconds }

        val lastBarUiWithActivity: BarUi? =
            barsUi.lastOrNull { it.intervalDb != null }
        val activeTimeFrom: Int? =
            if ((lastBarUiWithActivity != null) && (lastBarUiWithActivity == activityBarsUi.lastOrNull()))
                lastBarUiWithActivity.timeFinish
            else null

        return ActivityStats(
            activityDb = activityDb,
            intervalsSeconds = intervalsSeconds,
            activeTimeFrom = activeTimeFrom,
            barsCount = activityBarsUi.size,
        )
    }

    ///

    class BarUi(
        val intervalDb: IntervalDb?,
        val timeStart: Int,
        val seconds: Int,
    ) {
        val activityDb: ActivityDb? = intervalDb?.selectActivityDbCached()
        val ratio: Float = seconds.toFloat() / 86_400
        val timeFinish: Int = timeStart + seconds
    }

    data class ActivityStats(
        val activityDb: ActivityDb,
        val intervalsSeconds: Int,
        val activeTimeFrom: Int?,
        val barsCount: Int,
    ) {

        fun calcElapsedSeconds(): Int =
            intervalsSeconds + (activeTimeFrom?.let { time() - it } ?: 0)

        fun calcRestOfGoalTfTimerType(): TextFeatures.TimerType {
            val timerGoal: ActivityDb.GoalType.Timer? =
                activityDb.buildGoalTypeOrNull() as? ActivityDb.GoalType.Timer
            if (timerGoal == null) {
                reportApi("DayBarsUi.ActivityStats.calcRestOfGoalTfTimerType() Not Timer")
                return TextFeatures.TimerType.Stopwatch(startSeconds = 0)
            }
            val secondsLeft: Int =
                timerGoal.seconds - calcElapsedSeconds()
            if (secondsLeft > 0)
                return TextFeatures.TimerType.Timer(secondsLeft)
            return TextFeatures.TimerType.OverdueTimer(secondsLeft.absoluteValue)
        }
    }

    enum class DAY_STRING_FORMAT {
        ALL, EVEN,
    }

    ///

    companion object {

        suspend fun buildToday(): DayBarsUi {
            val utcOffset: Int = DayStartOffsetUtils.getLocalUtcOffsetCached()
            val todayDS: Int = UnixTime(utcOffset = utcOffset).localDay
            val barsUi: List<DayBarsUi> = buildList(
                dayStart = todayDS,
                dayFinish = todayDS,
                utcOffset = utcOffset,
            )
            return barsUi.first()
        }

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
                .selectBetweenTimeDesc(timeStart, timeFinish)
                .reversed()
                .toMutableList()

            // Previous interval
            IntervalDb.selectBetweenTimeDesc(0, timeStart - 1, 1).firstOrNull()?.let { prevIntervalDb ->
                intervalsAsc.add(0, prevIntervalDb) // 0 idx - to start
            }

            ///

            val now = time()
            val barDayFormat: DAY_STRING_FORMAT =
                if (dayStart == dayFinish) DAY_STRING_FORMAT.ALL else DAY_STRING_FORMAT.EVEN
            val daysBarsUi: List<DayBarsUi> = (dayStart..dayFinish).map { barUiDay ->
                val dayTimeStart: Int = UnixTime.byLocalDay(barUiDay, utcOffset).time
                val dayTimeFinish: Int = dayTimeStart + 86_400
                val dayMaxTimeFinish: Int = dayTimeFinish.limitMax(now)

                if ((now <= dayTimeStart) ||
                    intervalsAsc.isEmpty() ||
                    (dayTimeFinish <= intervalsAsc.first().time)
                ) {
                    return@map DayBarsUi(
                        unixDay = barUiDay,
                        barsUi = listOf(BarUi(null, dayTimeStart, 86_400)),
                        dayStringFormat = barDayFormat,
                    )
                }

                val firstInterval: IntervalDb = intervalsAsc.first()

                val dayBarsUi = mutableListOf<BarUi>()
                val dayIntervalsDb: List<IntervalDb> =
                    intervalsAsc.filter { it.time >= dayTimeStart && it.time < dayTimeFinish }

                // Adding leading section. Relevant for the beginning of history.
                if (firstInterval.time > dayTimeStart)
                    dayBarsUi.add(BarUi(null, dayTimeStart, firstInterval.time - dayTimeStart))
                else {
                    val todayFirstIntervalOrNull: IntervalDb? = dayIntervalsDb.firstOrNull()
                    if ((todayFirstIntervalOrNull == null) || (todayFirstIntervalOrNull.time > dayTimeStart)) {
                        val prevIntervalDb: IntervalDb =
                            intervalsAsc.last { it.time < dayTimeStart }
                        val seconds: Int =
                            (todayFirstIntervalOrNull?.time ?: dayMaxTimeFinish) - dayTimeStart
                        dayBarsUi.add(BarUi(prevIntervalDb, dayTimeStart, seconds))
                    }
                }

                // Adding other sections
                dayIntervalsDb.forEachIndexed { idx, intervalDb ->
                    val nextIntervalTime: Int =
                        dayIntervalsDb.getOrNull(idx + 1)?.time ?: dayMaxTimeFinish
                    val seconds: Int =
                        nextIntervalTime - intervalDb.time
                    dayBarsUi.add(BarUi(intervalDb, intervalDb.time, seconds))
                }

                // For today
                val trailingPadding: Int = dayTimeFinish - dayMaxTimeFinish
                if (trailingPadding > 0)
                    dayBarsUi.add(BarUi(null, dayMaxTimeFinish, trailingPadding))

                DayBarsUi(barUiDay, dayBarsUi, barDayFormat)
            }

            return daysBarsUi
        }
    }
}
