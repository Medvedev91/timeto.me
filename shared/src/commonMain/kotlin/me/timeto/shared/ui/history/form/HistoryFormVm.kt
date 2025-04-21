package me.timeto.shared.ui.history.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.launchExIo
import me.timeto.shared.misc.time
import me.timeto.shared.textFeatures
import me.timeto.shared.ui.DialogsManager
import me.timeto.shared.ui.UiException
import me.timeto.shared.vm.__Vm

class HistoryFormVm(
    initIntervalDb: IntervalDb?,
) : __Vm<HistoryFormVm.State>() {

    data class State(
        val initIntervalDb: IntervalDb?,
        val activityDb: ActivityDb?,
        val time: Int,
        val activitiesUi: List<ActivityUi>,
    ) {

        val title: String =
            if (initIntervalDb == null) "New Entry" else "Edit"
        val saveText: String =
            if (initIntervalDb == null) "Create" else "Save"

        val activityTitle = "Activity"

        val timeTitle = "Time Start"
        val timeNote: String =
            HistoryFormUtils.makeTimeNote(time, withToday = true)
    }

    override val state = MutableStateFlow(
        State(
            initIntervalDb = initIntervalDb,
            activityDb = initIntervalDb?.selectActivityDbCached(),
            time = initIntervalDb?.id ?: time(),
            activitiesUi = Cache.activitiesDbSorted.map { ActivityUi(activityDb = it) },
        )
    )

    fun setActivityDb(newActivityDb: ActivityDb?) {
        state.update { it.copy(activityDb = newActivityDb) }
    }

    fun setTime(newTime: Int) {
        state.update { it.copy(time = newTime) }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        val state = state.value
        val time: Int = state.time
        launchExIo {
            try {
                val activityDb: ActivityDb? = state.activityDb
                if (activityDb == null) {
                    dialogsManager.alert("Activity not selected")
                    return@launchExIo
                }
                val intervalDb: IntervalDb? = state.initIntervalDb
                if (intervalDb != null) {
                    intervalDb.updateEx(
                        newId = time,
                        newTimer = intervalDb.timer,
                        newActivityDb = activityDb,
                        newNote = intervalDb.note,
                    )
                } else {
                    IntervalDb.insertWithValidation(
                        timer = 45 * 60,
                        activityDb = activityDb,
                        note = null,
                        id = time,
                    )
                }
                onUi { onSuccess() }
            } catch (e: UiException) {
                dialogsManager.alert(e.uiMessage)
            }
        }
    }

    fun moveToTasks(
        intervalDb: IntervalDb,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        HistoryFormUtils.moveToTasksUi(
            intervalDb = intervalDb,
            dialogsManager = dialogsManager,
            onSuccess = {
                onUi { onSuccess() }
            },
        )
    }

    fun delete(
        intervalDb: IntervalDb,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        HistoryFormUtils.deleteIntervalUi(
            intervalDb = intervalDb,
            dialogsManager = dialogsManager,
            onSuccess = {
                onUi { onSuccess() }
            },
        )
    }

    ///

    data class ActivityUi(
        val activityDb: ActivityDb,
    ) {
        val title: String =
            activityDb.name.textFeatures().textNoFeatures
    }
}
