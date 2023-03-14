package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.DI
import timeto.shared.db.ActivityModel

class ActivityPickerSheetVM : __VM<ActivityPickerSheetVM.State>() {

    data class State(
        val activitiesUI: List<ActivityUI>,
    ) {
        val headerTitle = "Activity"
    }

    override val state = MutableStateFlow(
        State(
            activitiesUI = DI.activitiesSorted.map { ActivityUI(it) }
        )
    )

    ///

    class ActivityUI(
        val activity: ActivityModel,
    ) {
        // todo no triggers
        val text = activity.nameWithEmoji()
    }
}
