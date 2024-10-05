package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb

class ActivityPickerSheetVm : __Vm<ActivityPickerSheetVm.State>() {

    data class State(
        val activitiesUI: List<ActivityUI>,
    ) {
        val headerTitle = "Activity"
    }

    override val state = MutableStateFlow(
        State(
            activitiesUI = Cache.activitiesDbSorted.map { ActivityUI(it) }
        )
    )

    ///

    class ActivityUI(
        val activity: ActivityDb,
    ) {
        // todo no triggers
        val text = activity.nameWithEmoji()
    }
}
