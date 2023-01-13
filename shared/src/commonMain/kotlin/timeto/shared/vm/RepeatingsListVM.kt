package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.RepeatingModel

class RepeatingsListVM : __VM<RepeatingsListVM.State>() {

    class UiRepeating(
        val repeating: RepeatingModel,
    ) {
        val deletionNote = "Are you sure you want to delete \"${repeating.text}\"?"
        val dayLeftString = repeating.getPeriod().title
        val dayRightString = repeating.getNextDayString() + ", " + "${repeating.getNextDay() - UnixTime().localDay}d"
        val listText: String
        val triggers: List<Trigger>

        init {
            val textFeatures = TextFeatures.parse(repeating.text)
            listText = textFeatures.textUI()
            triggers = textFeatures.triggers
        }

        fun delete() {
            launchExDefault {
                repeating.delete()
            }
        }
    }

    data class State(
        val uiRepeatings: List<UiRepeating>,
    )

    override val state = MutableStateFlow(
        State(
            uiRepeatings = DI.repeatings.toUiList()
        )
    )

    override fun onAppear() {
        RepeatingModel.getAscFlow()
            .onEachExIn(scopeVM()) { list ->
                state.update { it.copy(uiRepeatings = list.toUiList()) }
            }
    }
}

private fun List<RepeatingModel>.toUiList() = this
    .sortedWith(compareBy<RepeatingModel> { it.getNextDay() }.thenByDescending { it.text.lowercase() })
    .map { RepeatingsListVM.UiRepeating(it) }
