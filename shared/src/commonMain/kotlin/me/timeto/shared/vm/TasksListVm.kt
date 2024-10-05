package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.EventDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.models.TaskUi
import me.timeto.shared.models.sortedUi

// todo live tmrw data?
class TasksListVm(
    val folder: TaskFolderDb,
) : __Vm<TasksListVm.State>() {

    data class State(
        val vmTasksUi: List<VmTaskUi>,
        val tmrwData: TmrwData?,
        val addFormInputTextValue: String,
    )

    override val state = MutableStateFlow(
        State(
            vmTasksUi = Cache.tasksDb.toUiList(),
            tmrwData = if (folder.isTmrw)
                prepTmrwData(
                    allRepeatings = Cache.repeatings,
                    allEvents = Cache.eventsDb,
                ) else null,
            addFormInputTextValue = "",
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        TaskDb.getAscFlow()
            .onEachExIn(scope) { list ->
                state.update { it.copy(vmTasksUi = list.toUiList()) }
            }
        // To update daytime badges
        scope.launch {
            while (true) {
                delayToNextMinute()
                state.update { it.copy(vmTasksUi = TaskDb.getAsc().toUiList()) }
            }
        }
    }

    fun setAddFormInputTextValue(text: String) = state.update {
        it.copy(addFormInputTextValue = text)
    }

    fun isAddFormInputEmpty() = state.value.addFormInputTextValue.isBlank()

    fun addTask(
        onSuccess: () -> Unit,
    ) = scopeVm().launchEx {
        try {
            TaskDb.addWithValidation(state.value.addFormInputTextValue, folder)
            setAddFormInputTextValue("")
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }

    private fun List<TaskDb>.toUiList() = this
        .filter { it.folder_id == folder.id }
        .map { TaskUi(it) }
        .sortedUi(isToday = folder.isToday)
        .map { VmTaskUi(it) }

    ///
    ///

    class TmrwData(
        val tasksUI: List<TmrwTaskUI>,
        val curTimeString: String,
    )

    class TmrwTaskUI(
        val task: TaskDb
    ) {

        val textFeatures = task.text.textFeatures()
        val text = textFeatures.textUi()

        val timeUI = textFeatures.calcTimeData()?.let { timeData ->
            val text = timeData.unixTime.getStringByComponents(
                UnixTime.StringComponent.dayOfMonth,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.month3,
                UnixTime.StringComponent.comma,
                UnixTime.StringComponent.space,
                UnixTime.StringComponent.hhmm24,
            )
            val textColor = if (timeData.type.isEvent())
                ColorRgba.blue else ColorRgba.textSecondary
            TmrwTimeUI(
                text = text,
                textColor = textColor,
            )
        }

        class TmrwTimeUI(
            val text: String,
            val textColor: ColorRgba,
        )
    }

    ///

    class VmTaskUi(
        val taskUi: TaskUi,
    ) {

        val text = taskUi.tf.textUi(withPausedEmoji = true)
        val timeUI: TimeUI? = taskUi.tf.calcTimeData()?.let { timeData ->
            val unixTime = timeData.unixTime
            val isHighlight = timeData.type.isEvent() || timeData._textFeatures.isImportant

            val timeLeftText = timeData.timeLeftText()
            val textColor = when (timeData.status) {
                TextFeatures.TimeData.STATUS.IN -> ColorRgba.textSecondary
                TextFeatures.TimeData.STATUS.SOON -> ColorRgba.blue
                TextFeatures.TimeData.STATUS.OVERDUE -> ColorRgba.red
            }

            if (isHighlight) {
                val backgroundColor = if (timeData.status.isOverdue())
                    ColorRgba.red else ColorRgba.blue
                return@let TimeUI.HighlightUI(
                    timeData = timeData,
                    title = timeData.timeText(),
                    backgroundColor = backgroundColor,
                    timeLeftText = timeLeftText,
                    timeLeftColor = textColor,
                )
            }

            val daytimeText = daytimeToString(unixTime.time - unixTime.localDayStartTime())
            TimeUI.RegularUI(
                timeData = timeData,
                text = "$daytimeText  $timeLeftText",
                textColor = textColor,
            )
        }
        val timerContext = ActivityTimerSheetVm.TimerContext.Task(taskUi.taskDb)

        fun upFolder(newFolder: TaskFolderDb) {
            launchExDefault {
                taskUi.taskDb.upFolder(newFolder, replaceIfTmrw = true)
            }
        }

        // - By manual removing;
        // - After adding to calendar;
        // - By starting from activity sheet;
        fun delete() {
            launchExDefault {
                taskUi.taskDb.delete()
            }
        }

        sealed class TimeUI(
            val _timeData: TextFeatures.TimeData,
        ) {

            class HighlightUI(
                timeData: TextFeatures.TimeData,
                val title: String,
                val backgroundColor: ColorRgba,
                val timeLeftText: String,
                val timeLeftColor: ColorRgba,
            ) : TimeUI(timeData)

            class RegularUI(
                timeData: TextFeatures.TimeData,
                val text: String,
                val textColor: ColorRgba,
            ) : TimeUI(timeData)
        }

    }
}

private fun prepTmrwData(
    allRepeatings: List<RepeatingDb>,
    allEvents: List<EventDb>,
): TasksListVm.TmrwData {

    val tasksDb = mutableListOf<TaskDb>()
    val unixTmrwDS = UnixTime(utcOffset = localUtcOffsetWithDayStart).inDays(1)
    val tmrwDSDay = unixTmrwDS.localDay
    var lastFakeTaskId = unixTmrwDS.localDayStartTime()

    // Repeatings
    allRepeatings
        .filter { it.getNextDay() == tmrwDSDay }
        .forEach { repeating ->
            tasksDb.add(
                TaskDb(
                    id = ++lastFakeTaskId,
                    text = repeating.prepTextForTask(tmrwDSDay),
                    folder_id = TaskFolderDb.ID_TODAY,
                )
            )
        }

    // Events
    allEvents
        .filter { it.getLocalTime().localDay == tmrwDSDay }
        .forEach { event ->
            tasksDb.add(
                TaskDb(
                    id = ++lastFakeTaskId,
                    text = event.prepTextForTask(),
                    folder_id = TaskFolderDb.ID_TODAY,
                )
            )
        }

    val resTasks = tasksDb
        .map { TaskUi(it) }
        .sortedUi(isToday = true)
        .map { TasksListVm.TmrwTaskUI(it.taskDb) }

    val curTimeString = unixTmrwDS.getStringByComponents(
        UnixTime.StringComponent.dayOfMonth,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.month,
        UnixTime.StringComponent.comma,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.dayOfWeek,
    )

    return TasksListVm.TmrwData(
        tasksUI = resTasks,
        curTimeString = "Tomorrow, $curTimeString"
    )
}
