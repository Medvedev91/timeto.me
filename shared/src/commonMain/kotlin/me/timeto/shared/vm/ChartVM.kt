package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.vm.ui.ActivitiesPeriodUI

class ChartVM(
    val activitiesUI: List<ActivitiesPeriodUI.ActivityUI>,
) : __VM<ChartVM.State>() {

    data class State(
        val dayStart: Int,
        val dayFinish: Int,
        val selectedId: String?,
        val pieItems: List<PieChart.ItemData>,
    )

    override val state: MutableStateFlow<State>

    init {
        val today = UnixTime().localDay
        state = MutableStateFlow(
            State(
                dayStart = today - 6,
                dayFinish = today,
                selectedId = null,
                pieItems = listOf(),
            )
        )
    }

    override fun onAppear() {
        upPeriod(state.value.dayStart, state.value.dayFinish)
    }

    fun selectId(id: String?) {
        state.update { it.copy(selectedId = id) }
    }

    fun upPeriod(
        dayStart: Int,
        dayFinish: Int,
    ) {
        scopeVM().launchEx {
            val items = activitiesUI.map { activityUI ->
                val activity = activityUI.activity
                val seconds = activityUI.seconds
                PieChart.ItemData(
                    id = "${activity.id}",
                    value = seconds.toDouble(),
                    color = activity.colorRgba,
                    title = activity.nameWithEmoji().textFeatures().textUi(),
                    shortTitle = activity.emoji,
                    subtitleTop = "${(activityUI.ratio * 100).toInt()}%",
                    subtitleBottom = secondsToString(seconds),
                    customData = activityUI.perDayString,
                )
            }
            state.update {
                it.copy(
                    dayStart = dayStart,
                    dayFinish = dayFinish,
                    pieItems = items,
                )
            }
        }
    }
}

private fun secondsToString(seconds: Int): String {
    val aTime = mutableListOf<String>()
    val hms = seconds.toHms()
    if (hms[0] > 0)
        aTime.add("${hms[0]}h")
    if (hms[1] > 0)
        aTime.add("${hms[1]}m")
    if (aTime.isEmpty())
        aTime.add("${hms[2]}s")
    return aTime.joinToString(" ")
}
