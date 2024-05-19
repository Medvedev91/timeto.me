package me.timeto.shared.libs

import me.timeto.shared.toHms

data class DaytimeModel(
    val hour: Int,
    val minute: Int,
) {

    val seconds: Int = (hour * 3_600) + (minute * 60)
    val text: String = hour.toString().padStart(2, '0') + ":" +
                       minute.toString().padStart(2, '0')

    companion object {

        fun byDaytime(daytime: Int): DaytimeModel {
            val (h, m) = daytime.toHms()
            return DaytimeModel(hour = h, minute = m)
        }
    }
}
