package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.db.ActivityModel
import timeto.shared.onEachExIn

class ActivityEmojiPickerVM : __VM<ActivityEmojiPickerVM.State>() {

    data class State(
        val activities: List<ActivityModel>,
    )

    override val state = MutableStateFlow(State(DI.activitiesSorted))

    override fun onAppear() {
        ActivityModel.getAscSortedFlow().onEachExIn(scopeVM()) { activities ->
            state.update { it.copy(activities = activities) }
        }
    }

    fun upText(text: String, activity: ActivityModel): String {
        val textFirstEmoji = state.value.activities
            .map { it.emoji to text.indexOf(it.emoji) }
            .filter { it.second != -1 }
            .minByOrNull { it.second }
            ?.first

        if (textFirstEmoji != null)
            return text.replace(textFirstEmoji, activity.emoji)

        return "${text.trim()} ${activity.emoji}"
    }
}
