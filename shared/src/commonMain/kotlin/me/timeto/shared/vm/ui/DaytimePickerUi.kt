package me.timeto.shared.vm.ui

data class DaytimePickerUi(
    val hour: Int,
    val minute: Int,
) {

    val minuteStepSlide = 5
    val minuteTicks: List<Tick> = (0..59).map {
        Tick(
            value = it,
            text = if ((it % minuteStepSlide) == 0) "$it" else null,
        )
    }

    data class Tick(
        val value: Int,
        val text: String?,
    )
}
