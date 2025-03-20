package me.timeto.shared.ui

import me.timeto.shared.launchExIo
import me.timeto.shared.toTimerHintNote

class TimerHintUi(
    val seconds: Int,
    val onStart: suspend () -> Unit,
) {

    val text: String =
        seconds.toTimerHintNote(isShort = true)

    fun startInterval(
        onSuccess: () -> Unit,
    ) {
        launchExIo {
            onStart()
            onSuccess()
        }
    }
}
