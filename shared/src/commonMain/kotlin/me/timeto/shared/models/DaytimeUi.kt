package me.timeto.shared.models

import me.timeto.shared.toHms

data class DaytimeUi(
    val hour: Int,
    val minute: Int,
) {

    val seconds: Int =
        (hour * 3_600) + (minute * 60)

    val text: String =
        hour.toString().padStart(2, '0') + ":" +
        minute.toString().padStart(2, '0')

    companion object {

        fun byDaytime(daytime: Int): DaytimeUi {
            val (h, m) = daytime.toHms()
            return DaytimeUi(hour = h, minute = m)
        }
    }
}
