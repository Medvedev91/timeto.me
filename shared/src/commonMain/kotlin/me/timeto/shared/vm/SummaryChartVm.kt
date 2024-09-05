package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*

class SummaryChartVm(
    val activitiesUI: List<SummarySheetVm.ActivityUI>,
) : __Vm<SummaryChartVm.State>() {

    data class State(
        val selectedId: String?,
        val pieItems: List<PieChart.ItemData>,
    )

    override val state = MutableStateFlow(
        State(
            selectedId = null,
            pieItems = listOf(),
        )
    )

    override fun onAppear() {
        scopeVM().launchEx {
            val items = activitiesUI.map { activityUI ->
                val activity = activityUI.activity
                val seconds = activityUI.seconds
                PieChart.ItemData(
                    id = "${activity.id}",
                    value = seconds.toDouble(),
                    color = activity.colorRgba,
                    title = activityUI.title,
                    shortTitle = activity.emoji,
                    subtitleTop = "${(activityUI.ratio * 100).toInt()}%",
                    subtitleBottom = activityUI.totalTimeString,
                    customData = activityUI.perDayString,
                )
            }
            state.update {
                it.copy(pieItems = items)
            }
        }
    }

    fun selectId(id: String?) {
        state.update { it.copy(selectedId = id) }
    }
}
