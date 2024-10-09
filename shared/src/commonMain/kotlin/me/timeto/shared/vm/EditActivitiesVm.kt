package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.ActivityDb

class EditActivitiesVm : __Vm<EditActivitiesVm.State>() {

    class ActivityUI(
        val activity: ActivityDb,
    ) {
        val listText = activity.nameWithEmoji().textFeatures().textUi()
    }

    data class State(
        val activitiesUI: List<ActivityUI>,
    )

    override val state = MutableStateFlow(
        State(activitiesUI = Cache.activitiesDbSorted.toUiList())
    )

    override fun onAppear() {
        val scope = scopeVm()
        ActivityDb.selectAllSortedFlow().onEachExIn(scope) { activities ->
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

private fun List<ActivityDb>.toUiList() = this
    .map { EditActivitiesVm.ActivityUI(it) }
