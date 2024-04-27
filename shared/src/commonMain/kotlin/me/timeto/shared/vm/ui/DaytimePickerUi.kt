package me.timeto.shared.vm.ui

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
            sliderStickText = if ((it % 2) == 0) "$it" else null,
            withSliderStick = true,
        )
    }

    val minuteStepSlide = 5
    val minuteTicks: List<Tick> = (0..59).map {
        val withSliderStickText = (it % minuteStepSlide) == 0
        Tick(
            value = it,
            sliderStickText = if (withSliderStickText) "$it" else null,
            withSliderStick = withSliderStickText,
        )
    }

    ///

    data class Tick(
        val value: Int,
        val sliderStickText: String?,
        val withSliderStick: Boolean,
    ) {
        val text = value.toString().padStart(2, '0')
    }
}
