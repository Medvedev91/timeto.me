package me.timeto.shared.vm.zen_mode

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.TimerStateUi
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.Vm
import kotlin.time.Duration.Companion.milliseconds

class ZenModeVm : Vm<ZenModeVm.State>() {

    data class State(
        val intervalDb: IntervalDb,
        val allTasksDb: List<TaskDb>,
        val idToUpdate: Int,
    ) {
        val timerStateUi = TimerStateUi(
            intervalDb = intervalDb,
            todayTasksDb = allTasksDb.filter { it.isToday },
            isPurple = false,
        )
    }

    override val state = MutableStateFlow(
        State(
            intervalDb = Cache.lastIntervalDb,
            allTasksDb = Cache.tasksDb,
            idToUpdate = 0,
        )
    )

    init {
        val scopeVm = scopeVm()

        scopeVm.launch {
            while (true) {
                state.update {
                    it.copy(
                        intervalDb = Cache.lastIntervalDb,
                        idToUpdate = it.idToUpdate + 1, // Force update
                    )
                }
                delay(1_000.milliseconds)
            }
        }

        combine(
            IntervalDb.selectLastOneOrNullFlow().filterNotNull(),
            TaskDb.selectAscFlow(),
        ) { lastIntervalDb, allTasksDb ->
            state.update {
                it.copy(
                    intervalDb = lastIntervalDb,
                    allTasksDb = allTasksDb,
                )
            }
        }.launchIn(scopeVm)
    }
}
