package me.timeto.shared.vm.summary

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.PieChart
import me.timeto.shared.launchEx
import me.timeto.shared.vm.Vm

class SummaryChartVm(
    activitiesUi: List<SummaryVm.GoalUi>,
) : Vm<SummaryChartVm.State>() {

    data class State(
        val pieItems: List<PieChart.ItemData>,
    )

    override val state = MutableStateFlow(
        State(
            pieItems = emptyList(),
        )
    )

    init {
        scopeVm().launchEx {
            val items = activitiesUi.map { goalUi ->
                val goalDb = goalUi.goalDb
                val seconds = goalUi.seconds
                PieChart.ItemData(
                    id = "${goalDb.id}",
                    value = seconds.toDouble(),
                    color = goalDb.colorRgba,
                    title = goalUi.title,
                    shortTitle = goalUi.title,
                    subtitleTop = "${(goalUi.ratio * 100).toInt()}%",
                    subtitleBottom = goalUi.totalTimeString,
                    customData = goalUi.perDayString,
                )
            }
            state.update {
                it.copy(pieItems = items)
            }
        }
    }
}
