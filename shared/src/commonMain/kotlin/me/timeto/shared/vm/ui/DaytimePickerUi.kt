package me.timeto.shared.vm.ui

data class DaytimePickerUi(
    val hour: Int,
    val minute: Int,
) {

    val minuteTicks: List<Tick> = listOf()
    val minuteStepSlide = 5

    data class Tick(
        val value: Int,
        val hint: String?,
    )
}
