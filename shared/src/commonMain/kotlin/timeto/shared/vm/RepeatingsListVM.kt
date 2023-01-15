package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.RepeatingModel
import timeto.shared.vm.ui.DaytimeUI

class RepeatingsListVM : __VM<RepeatingsListVM.State>() {

    class RepeatingUI(
        val repeating: RepeatingModel,
    ) {
        val deletionNote = "Are you sure you want to delete \"${repeating.text}\"?"
        val dayLeftString = repeating.getPeriod().title
        val dayRightString = repeating.getNextDayString() + ", " + "${repeating.getNextDay() - UnixTime().localDay}d"

        val listText: String
        val triggers: List<Trigger>
        val daytimeUI: DaytimeUI?

        init {
            val textFeatures = TextFeatures.parse(repeating.text)
            listText = textFeatures.textUI()
            triggers = textFeatures.triggers
            daytimeUI = textFeatures.toDaytimeUIOrNull(withTimeLeft = false)
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
    .sortedWith(compareBy<RepeatingModel> { it.getNextDay() }.thenByDescending { it.text.lowercase() })
    .map { RepeatingsListVM.RepeatingUI(it) }
