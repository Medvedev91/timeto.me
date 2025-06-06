package me.timeto.shared.vm.history.form

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.launchExIo
import me.timeto.shared.time
import me.timeto.shared.textFeatures
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.vm.Vm

class HistoryFormVm(
    initIntervalDb: IntervalDb?,
) : Vm<HistoryFormVm.State>() {

    private val initTime: Int = initIntervalDb?.id ?: time()

    data class State(
        val initIntervalDb: IntervalDb?,
        val activityDb: ActivityDb?,
        val time: Int,
        val activitiesUi: List<ActivityUi>,
        val timerItemsUi: List<TimerItemUi>,
    ) {

        val title: String =
            if (initIntervalDb == null) "New Entry"
            else {
                val note: String? = initIntervalDb.note
                    ?.trim()
                    ?.textFeatures()
                    ?.textNoFeatures
                    ?.takeIf { it.isNotBlank() }
                note ?: initIntervalDb.selectActivityDbCached().name.textFeatures().textNoFeatures
            }
        val doneText: String =
            if (initIntervalDb == null) "Create" else "Save"

        val activityTitle = "Activity"
        val activityNote: String =
            activityDb?.name?.textFeatures()?.textNoFeatures ?: "None"
    }

    override val state = MutableStateFlow(
        State(
            initIntervalDb = initIntervalDb,
            activityDb = initIntervalDb?.selectActivityDbCached(),
            time = initTime,
            activitiesUi = Cache.activitiesDbSorted.map { ActivityUi(activityDb = it) },
            timerItemsUi = makeTimerItemsUi(selectedTime = initTime),
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

    data class TimerItemUi(
        val time: Int,
    ) {
        val title: String =
            HistoryFormUtils.makeTimeNote(time, withToday = false)
    }
}

private fun makeTimerItemsUi(
    selectedTime: Int,
): List<HistoryFormVm.TimerItemUi> {
    val secondsSet: MutableSet<Int> = mutableSetOf(selectedTime)

    val timeStart1Min: Int = selectedTime - (selectedTime % 60)
    for (i in 1 until 10) { // 10 minutes
        secondsSet.add(timeStart1Min + (i * 60))
        secondsSet.add(timeStart1Min - (i * 60))
    }

    val timeStart5Min: Int = selectedTime - (selectedTime % (60 * 5))
    for (i in 1 until 12) { // ~ 1 hour
        secondsSet.add(timeStart5Min + (i * 60 * 5))
        secondsSet.add(timeStart5Min - (i * 60 * 5))
    }

    val timeStart10Min: Int = selectedTime - (selectedTime % (60 * 10))
    for (i in 1 until 144) { // ~ 1 day
        secondsSet.add(timeStart10Min + (i * 60 * 10))
        secondsSet.add(timeStart10Min - (i * 60 * 10))
    }

    val now: Int = time()
    return secondsSet.filter { it <= now }.sorted().map { time ->
        HistoryFormVm.TimerItemUi(time)
    }
}
