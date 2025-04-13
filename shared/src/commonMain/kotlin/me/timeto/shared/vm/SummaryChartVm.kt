package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.ui.summary.SummaryVm

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
