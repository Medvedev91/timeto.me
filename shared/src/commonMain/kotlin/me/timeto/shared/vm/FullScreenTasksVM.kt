package me.timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.ColorNative
import me.timeto.shared.DI
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.ui.TimerDataUI

class FullScreenTasksVM : __VM<FullScreenTasksVM.State>() {

    data class State(
        val title: String,
        val timerData: TimerDataUI,
    )

    override val state = MutableStateFlow(prepState())

    override fun onAppear() {
        val scope = scopeVM()
        scope.launch {
            while (true) {
                state.update { prepState() }
                delay(1_000L)
            }
        }
    }

    companion object {

        private fun prepState(): State {
            val interval = DI.lastInterval
            val text = interval.note ?: interval.getActivityDI().name
            val textFeatures = text.textFeatures()
            return State(
                title = textFeatures.textUi(withActivityEmoji = false, withTimer = false),
                timerData = TimerDataUI(interval, true, ColorNative.white)
            )
        }
    }
}
