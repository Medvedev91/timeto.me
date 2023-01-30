package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.TextFeatures
import timeto.shared.db.ActivityModel
import timeto.shared.onEachExIn

class ActivityEmojiPickerVM : __VM<ActivityEmojiPickerVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
    ) {
        val listText = TextFeatures.parse(activity.nameWithEmoji()).textUI()
    }

    data class State(
        val activitiesUI: List<ActivityUI>,
    )

    override val state = MutableStateFlow(State(DI.activitiesSorted.toUiList()))

    override fun onAppear() {
        ActivityModel.getAscSortedFlow().onEachExIn(scopeVM()) { activities ->
            state.update { it.copy(activitiesUI = activities.toUiList()) }
        }
    }

    fun upText(text: String, activity: ActivityModel): String {
        val textFirstEmoji = state.value.activitiesUI
            .map { it.activity }
            .map { it.emoji to text.indexOf(it.emoji) }
            .filter { it.second != -1 }
            .minByOrNull { it.second }
            ?.first

        if (textFirstEmoji != null)
            return text.replace(textFirstEmoji, activity.emoji)

        return "${text.trim()} ${activity.emoji}"
    }
}

private fun List<ActivityModel>.toUiList() = this.map { ActivityEmojiPickerVM.ActivityUI(it) }
