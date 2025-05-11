package me.timeto.shared.ui.tasks.tab.repeatings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.UnixTime
import me.timeto.shared.daytimeToString
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.__Vm

class TasksTabRepeatingsVm : __Vm<TasksTabRepeatingsVm.State>() {

    data class State(
        val repeatingsUi: List<RepeatingUi>,
    )

    override val state = MutableStateFlow(
        State(
            repeatingsUi = Cache.repeatingsDb.toUiList(),
        )
    )

    init {
        val scopeVm = scopeVm()
        RepeatingDb.selectAscFlow().onEachExIn(scopeVm) { repeatingsDb ->
            state.update { it.copy(repeatingsUi = repeatingsDb.toUiList()) }
        }
    }

    ///

    data class RepeatingUi(
        val repeatingDb: RepeatingDb,
    ) {

        val dayLeftString: String =
            repeatingDb.getPeriod().title +
            (repeatingDb.daytime?.let { " at ${daytimeToString(it)}" } ?: "")

        val dayRightString: String =
            "${repeatingDb.getNextDayString()}, " +
            "${repeatingDb.getNextDay() - UnixTime().localDay}d"

        val textFeatures: TextFeatures =
            repeatingDb.text.textFeatures()

        val listText: String =
            textFeatures.textUi()
    }
}

private fun List<RepeatingDb>.toUiList(
): List<TasksTabRepeatingsVm.RepeatingUi> = this
    .groupBy { it.getNextDay() }
    .toList()
    .sortedBy { it.first }
    .map { it.second.sortedInsideDay() }
    .flatten()
    .map { TasksTabRepeatingsVm.RepeatingUi(it) }

private fun List<RepeatingDb>.sortedInsideDay(
): List<RepeatingDb> {
    val (withDaytime, noDaytime) = this.partition { it.daytime != null }

    val resList = mutableListOf<RepeatingDb>()
    noDaytime
        .sortedByDescending { it.id }
        .forEach { resList.add(it) }
    withDaytime
        .sortedBy { it.daytimeToTimeWithDayStart(123) } // 123 - regardless of the day
        .forEach { resList.add(it) }

    return resList
}
