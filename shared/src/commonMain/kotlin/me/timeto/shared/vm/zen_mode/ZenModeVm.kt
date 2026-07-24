package me.timeto.shared.vm.zen_mode

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.IntervalUi
import me.timeto.shared.TimerStateUi
import me.timeto.shared.UnixTime
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.vm.Vm
import kotlin.time.Duration.Companion.milliseconds

class ZenModeVm : Vm<ZenModeVm.State>() {

    data class State(
        val intervalUi: IntervalUi,
        val allTasksDb: List<TaskDb>,
        val idToUpdate: Int,
    ) {

        val timerStateUi = TimerStateUi(
            intervalUi = intervalUi,
            todayTasksDb = allTasksDb.filter { it.isToday },
            isPurple = false,
        )

        val checklistDb: ChecklistDb? =
            timerStateUi.tfForTriggers.checklistsDb.firstOrNull()

        val dateText: String = UnixTime().getStringByComponents(
            UnixTime.StringComponent.hhmm24,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfWeek3,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.month3,
        )
    }

    override val state = run {
        val intervalDb: IntervalDb = Cache.lastIntervalDb
        MutableStateFlow(
            State(
                intervalUi = IntervalUi(
                    intervalDb = intervalDb,
                    activityDb = intervalDb.selectActivityDbCached(),
                ),
                allTasksDb = Cache.tasksDb,
                idToUpdate = 0,
            )
        )
    }

    init {
        val scopeVm = scopeVm()

        scopeVm.launch {
            while (true) {
                state.update { it.copy(idToUpdate = it.idToUpdate + 1) }
                delay(1_000.milliseconds)
            }
        }

        combine(
            IntervalDb.selectLastOneOrNullFlow().filterNotNull(),
            TaskDb.selectAscFlow(),
        ) { lastIntervalDb, allTasksDb ->
            state.update {
                it.copy(
                    intervalUi = IntervalUi(
                        intervalDb = lastIntervalDb,
                        activityDb = lastIntervalDb.selectActivityDb(),
                    ),
                    allTasksDb = allTasksDb,
                )
            }
        }.launchIn(scopeVm)
    }
}
