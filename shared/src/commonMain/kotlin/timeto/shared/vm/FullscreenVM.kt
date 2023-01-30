package timeto.shared.vm

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timeto.shared.*
import timeto.shared.db.IntervalModel

class FullscreenVM(
    private val defColor: ColorNative,
) : __VM<FullscreenVM.State>() {

    data class State(
        val title: String,
        val timerData: TimerData,
    )

    override val state = MutableStateFlow(prepState(DI.lastInterval, defColor))

    override fun onAppear() {
        val scope = scopeVM()
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) {
                upState(it)
            }
        scope.launch {
            while (true) {
                upState(DI.lastInterval)
                delay(1_000L)
            }
        }
    }

    private fun upState(interval: IntervalModel) {
        state.update { prepState(interval, defColor) }
    }
}

private fun prepState(
    lastInterval: IntervalModel,
    defColor: ColorNative,
): FullscreenVM.State {
    val title = lastInterval.note ?: DI.activitiesSorted.first { it.id == lastInterval.activity_id }.nameWithEmoji()
    val timerData = TimerData(lastInterval, defColor)
    return FullscreenVM.State(
        title = title,
        timerData = timerData,
    )
}
