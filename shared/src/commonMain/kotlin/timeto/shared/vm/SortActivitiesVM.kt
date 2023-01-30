package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.TextFeatures
import timeto.shared.db.ActivityModel
import timeto.shared.launchExDefault
import timeto.shared.onEachExIn

class SortActivitiesVM : __VM<SortActivitiesVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
    ) {

        val listText = TextFeatures.parse(activity.nameWithEmoji()).textUI()
    }

    data class State(
        val activitiesUI: List<ActivityUI>,
    )

    override val state = MutableStateFlow(
        State(activitiesUI = DI.activitiesSorted.toUiList())
    )

    override fun onAppear() {
        val scope = scopeVM()
        ActivityModel.getAscSortedFlow().onEachExIn(scope) { activities ->
            state.update { it.copy(activitiesUI = activities.toUiList()) }
        }
    }

    ///
    /// Up / Down

    fun up(activityUI: ActivityUI) {
        val tmpActivities = state.value.activitiesUI.toMutableList()
        val curIndex = tmpActivities.indexOf(activityUI)
        if (curIndex == 0)
            return

        val prevItemIndex = curIndex - 1
        val prevItem = tmpActivities[prevItemIndex]
        tmpActivities[prevItemIndex] = activityUI
        tmpActivities[curIndex] = prevItem

        launchExDefault {
            tmpActivities.forEachIndexed { newIndex, activityUI ->
                activityUI.activity.upSort(newIndex)
            }
        }
    }

    fun down(activityUI: ActivityUI) {
        val tmpActivities = state.value.activitiesUI.toMutableList()
        val curIndex = tmpActivities.indexOf(activityUI)
        if ((curIndex + 1) == tmpActivities.size)
            return

        val nextItemIndex = curIndex + 1
        val nexItem = tmpActivities[nextItemIndex]
        tmpActivities[nextItemIndex] = activityUI
        tmpActivities[curIndex] = nexItem

        launchExDefault {
            tmpActivities.forEachIndexed { newIndex, activityUI ->
                activityUI.activity.upSort(newIndex)
            }
        }
    }
}

private fun List<ActivityModel>.toUiList() = this
    .map { SortActivitiesVM.ActivityUI(it) }
