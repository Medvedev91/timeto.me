package me.timeto.shared

import me.timeto.shared.db.Goal2Db

data class DaytimeUi(
    val hour: Int,
    val minute: Int,
) {

    val seconds: Int =
        (hour * 3_600) + (minute * 60)

    val text: String =
        hour.toString().padStart(2, '0') + ":" + minute.toString().padStart(2, '0')

    // region Start Until

    fun startUntilAsync(goalDb: Goal2Db) {
        launchExIo {
            startUntil(goalDb)
        }
    }

    suspend fun startUntil(goalDb: Goal2Db) {
        val unixTimeNow = UnixTime()
        val timeNow: Int = unixTimeNow.time
        val dayStartNow: Int = unixTimeNow.localDayStartTime()
        val finishTimeTmp: Int = dayStartNow + this.seconds
        // Today / Tomorrow
        val finishTime: Int =
            if (finishTimeTmp > timeNow) finishTimeTmp
            else finishTimeTmp + (3_600 * 24)
        val newTimer = finishTime - timeNow
        goalDb.startInterval(newTimer)
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
