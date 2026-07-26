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
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.launchExIo
import me.timeto.shared.vm.Vm
import kotlin.time.Duration.Companion.milliseconds

class ZenModeVm : Vm<ZenModeVm.State>() {

    data class State(
        val intervalUi: IntervalUi,
        val allTasksDb: List<TaskDb>,
        val initShowChecklist: Boolean,
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
                initShowChecklist = intervalDb.activityId !in parseHiddenActivityIds(
                    raw = KvDb.KEY.ZEN_MODE_CHECKLISTS_VISIBILITY.selectStringOrNullCached()
                ),
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

    fun showChecklist() {
        launchExIo {
            val activityId: Int =
                state.value.intervalUi.activityDb.id
            val activityIds: MutableSet<Int> =
                getHiddenActivityIds().toMutableSet()
            activityIds.remove(activityId)
            saveHiddenActivityIds(activityIds)
        }
    }

    fun hideChecklist() {
        launchExIo {
            val activityId: Int =
                state.value.intervalUi.activityDb.id
            val activityIds: MutableSet<Int> =
                getHiddenActivityIds().toMutableSet()
            activityIds.add(activityId)
            saveHiddenActivityIds(activityIds)
        }
    }

    private suspend fun getHiddenActivityIds(): Set<Int> {
        val raw: String? =
            KvDb.KEY.ZEN_MODE_CHECKLISTS_VISIBILITY.selectStringOrNull()
        return parseHiddenActivityIds(raw)
    }

    private fun parseHiddenActivityIds(raw: String?): Set<Int> {
        return (raw ?: "").split(",").mapNotNull { it.toIntOrNull() }.toSet()
    }

    private suspend fun saveHiddenActivityIds(activityIds: Set<Int>) {
        val raw: String = activityIds.joinToString(",")
        KvDb.KEY.ZEN_MODE_CHECKLISTS_VISIBILITY.upsertString(raw)
    }
}
