package me.timeto.shared.ui

import me.timeto.shared.db.ActivityModel
import me.timeto.shared.launchExDefault
import me.timeto.shared.toTimerHintNote

class TimerHintUI(
    val seconds: Int,
    val activity: ActivityModel,
    val isPrimary: Boolean,
    private val onStart: suspend (Int) -> Unit,
) {

    companion object {

        fun buildList(
            activity: ActivityModel,
            historyLimit: Int,
            customLimit: Int,
            primaryHints: List<Int> = listOf(),
            onStart: suspend (Int) -> Unit,
        ): List<TimerHintUI> {
            return activity
                .getData()
                .timer_hints
                .get(
                    historyLimit = historyLimit,
                    customLimit = customLimit,
                    primaryHints = primaryHints,
                ).map { seconds ->
                    TimerHintUI(
                        seconds = seconds,
                        activity = activity,
                        isPrimary = seconds in primaryHints,
                        onStart = onStart,
                    )
                }
        }
    }

    val text = seconds.toTimerHintNote(isShort = true)

    fun startInterval(
        onSuccess: (() -> Unit) = {},
    ) {
        launchExDefault {
            onStart(seconds)
            onSuccess()
        }
    }
}
