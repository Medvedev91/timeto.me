package me.timeto.shared

import me.timeto.shared.db.ActivityDb

data class DaytimeUi(
    val hour: Int,
    val minute: Int,
) {

    val seconds: Int =
        (hour * 3_600) + (minute * 60)

    val text: String =
        hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')

    fun calcTimer(): TextFeatures.TimerType.Timer {
        val unixTimeNow = UnixTime()
        val timeNow: Int = unixTimeNow.time
        val dayStartNow: Int = unixTimeNow.localDayStartTime()
        val finishTimeTmp: Int = dayStartNow + this.seconds
        // Today / Tomorrow
        val finishTime: Int =
            if (finishTimeTmp > timeNow) finishTimeTmp
            else finishTimeTmp + (3_600 * 24)
        return TextFeatures.TimerType.Timer(seconds = finishTime - timeNow)
    }

    // region Start Until

    fun startUntilAsync(activityDb: ActivityDb) {
        launchExIo {
            startUntil(activityDb)
        }
    }

    suspend fun startUntil(activityDb: ActivityDb) {
        activityDb.startTimer(calcTimer().seconds)
    }

    // endregion

    companion object {

        fun now(): DaytimeUi {
            val unixTime = UnixTime()
            return byDaytime(unixTime.time - unixTime.localDayStartTime())
        }

        fun byDaytime(daytime: Int): DaytimeUi {
            val (h, m) = daytime.toHms()
            return DaytimeUi(hour = h, minute = m)
        }
    }
}
