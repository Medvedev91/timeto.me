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

    override val state: MutableStateFlow<State>

    init {
        val prepState = prepState(DI.lastInterval, defColor)
        state = MutableStateFlow(
            State(
                title = prepState.first,
                timerData = prepState.second,
            )
        )
    }

    override fun onAppear() {
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scopeVM()) {
                upState(it)
            }
        scopeVM().launch {
            while (true) {
                upState(DI.lastInterval)
                delay(1_000L)
            }
        }
    }

    private fun upState(interval: IntervalModel) {
        val prepState = prepState(interval, defColor)
        state.update {
            it.copy(
                title = prepState.first,
                timerData = prepState.second,
            )
        }
    }
}

private fun prepState(
    lastInterval: IntervalModel,
    defColor: ColorNative,
): Pair<String, TimerData> {
    val title = lastInterval.note ?: DI.activitiesSorted.first { it.id == lastInterval.activity_id }.nameWithEmoji()
    val timerData = TimerData(lastInterval, defColor)
    return title to timerData
}
