package me.timeto.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetIn

val localUtcOffsetFlow: MutableStateFlow<Int> =
    MutableStateFlow(calcLocalUtcOffset())

fun localUtcOffsetFlowUpdate() {
    localUtcOffsetFlow.tryEmit(calcLocalUtcOffset())
}

private fun calcLocalUtcOffset(): Int =
    Clock.System.now().offsetIn(TimeZone.currentSystemDefault()).totalSeconds
