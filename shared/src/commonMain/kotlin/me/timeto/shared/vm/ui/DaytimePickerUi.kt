package me.timeto.shared.vm.ui

import me.timeto.shared.toHms

data class DaytimePickerUi(
    val hour: Int,
    val minute: Int,
) {

    val text: String = "$hour".padStart(2, '0') + ":" + "$minute".padStart(2, '0')
    val seconds: Int = (hour * 3_600) + (minute * 60)

    val hourStepSlide = 1
    val hourTicks: List<Tick> = (0..23).map {
        Tick(
            value = it,
            text = if ((it % 2) == 0) "$it" else null,
            withSliderStick = true,
        )
    }

    val minuteStepSlide = 5
    val minuteTicks: List<Tick> = (0..59).map {
        val withText = (it % minuteStepSlide) == 0
        Tick(
            value = it,
            text = if (withText) "$it" else null,
            withSliderStick = withText,
        )
    }

    ///

    companion object {

        fun byHms(daytime: Int): DaytimePickerUi {
            val (h, m) = daytime.toHms()
            return DaytimePickerUi(
                hour = h,
                minute = m,
            )
        }
    }

    data class Tick(
        val value: Int,
        val text: String?,
        val withSliderStick: Boolean,
    )
}
