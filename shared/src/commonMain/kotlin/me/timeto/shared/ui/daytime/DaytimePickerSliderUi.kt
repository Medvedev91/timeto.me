package me.timeto.shared.ui.daytime

import me.timeto.shared.limitMax
import me.timeto.shared.limitMinMax

data class DaytimePickerSliderUi(
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

    companion object {

        fun calcSliderTickIdx(
            ticksSize: Int,
            stepTicks: Int,
            slideXPosition: Float,
            stepPx: Float,
        ): Int {
            val prevStep = slideXPosition / stepPx
            val prevStepExtra = prevStep - prevStep.toInt()
            val newStep = (if (prevStepExtra < 0.5) prevStep else prevStep + 1)
                .toInt()
                .limitMax((ticksSize - 1) / stepTicks)
            val newIdx = (newStep * stepTicks).limitMinMax(min = 0, max = ticksSize - 1)
            return newIdx
        }
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
