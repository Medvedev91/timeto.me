package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.RepeatingModel

class RepeatingsListVM : __VM<RepeatingsListVM.State>() {

    class RepeatingUI(
        val repeating: RepeatingModel,
    ) {

        val deletionNote = "Are you sure you want to delete \"${repeating.text}\"?"
        val dayLeftString: String
        val dayRightString = repeating.getNextDayString() + ", " + "${repeating.getNextDay() - UnixTime().localDay}d"

        val textFeatures = repeating.text.textFeatures()
        val listText = textFeatures.textUi()

        init {
            val daytimeText = repeating.daytime?.let { " at ${daytimeToString(it)}" } ?: ""
            dayLeftString = repeating.getPeriod().title + daytimeText
        }

        fun delete() {
            launchExDefault {
                repeating.delete()
            }
        }
    }

    data class State(
        val repeatingsUI: List<RepeatingUI>,
    )

    override val state = MutableStateFlow(
        State(
            repeatingsUI = DI.repeatings.toUiList()
        )
    )

    override fun onAppear() {
        RepeatingModel.getAscFlow()
            .onEachExIn(scopeVM()) { list ->
                state.update { it.copy(repeatingsUI = list.toUiList()) }
            }
    }
}

private fun List<RepeatingModel>.toUiList() = this
    .groupBy { it.getNextDay() }
    .toList()
    .sortedBy { it.first }
    .map { it.second.sortedInsideDay() }
    .flatten()
    .map { RepeatingsListVM.RepeatingUI(it) }

private fun List<RepeatingModel>.sortedInsideDay(): List<RepeatingModel> {
    val (withDaytime, noDaytime) = this.partition { it.daytime != null }

    val resList = mutableListOf<RepeatingModel>()
    noDaytime
        .sortedByDescending { it.id }
        .forEach { resList.add(it) }
    withDaytime
        .sortedBy { it.daytimeToTimeWithDayStart(123) } // 123 - regardless of the day
        .forEach { resList.add(it) }

    return resList
}
