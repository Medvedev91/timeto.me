package timeto.shared.vm.ui

import timeto.shared.db.ActivityModel
import timeto.shared.launchExDefault
import timeto.shared.toTimerHintNote

class TimerHintUI(
    val seconds: Int,
    val activity: ActivityModel,
    isShort: Boolean,
    private val onStart: suspend (Int) -> Unit,
) {

    companion object {

        fun buildList(
            activity: ActivityModel,
            isShort: Boolean,
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
                    TimerHintUI(seconds, activity, isShort, onStart)
                }
        }
    }

    val text = seconds.toTimerHintNote(isShort = isShort)

    fun startInterval(
        onSuccess: (() -> Unit) = {},
    ) {
        launchExDefault {
            onStart(seconds)
            onSuccess()
        }
    }
}
