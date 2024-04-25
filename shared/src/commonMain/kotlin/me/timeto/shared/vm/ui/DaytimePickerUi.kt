package me.timeto.shared.vm.ui

data class DaytimePickerUi(
    val hour: Int,
    val minute: Int,
) {

    val hourStepSlide = 1
    val hourTicks: List<Tick> = (0..23).map {
        Tick(
            value = it,
            text = if ((it % 2) == 0) "$it" else null,
            withStick = true,
        )
    }

    val minuteStepSlide = 5
    val minuteTicks: List<Tick> = (0..59).map {
        val withText = (it % minuteStepSlide) == 0
        Tick(
            value = it,
            text = if (withText) "$it" else null,
            withStick = withText,
        )
    }

    data class Tick(
        val value: Int,
        val text: String?,
        val withStick: Boolean,
    )
}
