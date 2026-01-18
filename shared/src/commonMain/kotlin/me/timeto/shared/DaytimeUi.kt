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

    fun startUntilAsync(goalDb: Goal2Db) {
        launchExIo {
            goalDb.startIntervalUntilDaytime(this@DaytimeUi)
        }
    }

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
