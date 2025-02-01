package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.misc.toBoolean10

class RepeatingsListVm : __Vm<RepeatingsListVm.State>() {

    class RepeatingUI(
        val repeating: RepeatingDb,
    ) {

        val isImportant = repeating.is_important.toBoolean10()

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
            repeatingsUI = Cache.repeatingsDb.toUiList()
        )
    )

    override fun onAppear() {
        RepeatingDb.getAscFlow()
            .onEachExIn(scopeVm()) { list ->
                state.update { it.copy(repeatingsUI = list.toUiList()) }
            }
    }
}

private fun List<RepeatingDb>.toUiList() = this
    .groupBy { it.getNextDay() }
    .toList()
    .sortedBy { it.first }
    .map { it.second.sortedInsideDay() }
    .flatten()
    .map { RepeatingsListVm.RepeatingUI(it) }

private fun List<RepeatingDb>.sortedInsideDay(): List<RepeatingDb> {
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
