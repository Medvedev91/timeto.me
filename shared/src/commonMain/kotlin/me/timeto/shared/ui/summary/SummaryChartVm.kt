package me.timeto.shared.ui.summary

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.misc.PieChart
import me.timeto.shared.launchEx
import me.timeto.shared.ui.__Vm

class SummaryChartVm(
    activitiesUi: List<SummaryVm.ActivityUi>,
) : __Vm<SummaryChartVm.State>() {

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
            val items = activitiesUi.map { activityUi ->
                val activity = activityUi.activity
                val seconds = activityUi.seconds
                PieChart.ItemData(
                    id = "${activity.id}",
                    value = seconds.toDouble(),
                    color = activity.colorRgba,
                    title = activityUi.title,
                    shortTitle = activity.emoji,
                    subtitleTop = "${(activityUi.ratio * 100).toInt()}%",
                    subtitleBottom = activityUi.totalTimeString,
                    customData = activityUi.perDayString,
                )
            }
            state.update {
                it.copy(pieItems = items)
            }
        }
    }
}
