package me.timeto.shared.ui.activities

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.textFeatures
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.vm.__Vm

class ActivityFormVm(
    initActivityDb: ActivityDb?,
) : __Vm<ActivityFormVm.State>() {

    data class State(
        val activityDb: ActivityDb?,
        val name: String,
    ) {

        val title: String =
            if (activityDb != null) "Edit Activity" else "New Activity"

        val saveText: String =
            if (activityDb != null) "Save" else "Create"

        val nameHeader = "ACTIVITY NAME"
        val namePlaceholder = "Activity Name"
    }

    override val state: MutableStateFlow<State>

    init {

        val tf: TextFeatures =
            (initActivityDb?.name ?: "").textFeatures()

        state = MutableStateFlow(
            State(
                activityDb = initActivityDb,
                name = tf.textNoFeatures,
            )
        )
    }

    ///

    fun setName(newName: String) {
        state.update { it.copy(name = newName) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSave: () -> Unit,
    ) {
        TODO()
    }
}
