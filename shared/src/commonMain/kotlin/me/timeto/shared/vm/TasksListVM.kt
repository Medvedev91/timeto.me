package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.EventDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.TaskFolderModel
import me.timeto.shared.db.TaskModel
import me.timeto.shared.vm.ui.sortedByFolder

// todo live tmrw data?
class TasksListVM(
    val folder: TaskFolderModel,
) : __VM<TasksListVM.State>() {

    data class State(
        val tasksUI: List<TaskUI>,
        val tmrwData: TmrwData?,
        val addFormInputTextValue: String,
    )

    override val state = MutableStateFlow(
        State(
            tasksUI = DI.tasks.toUiList(),
            tmrwData = if (folder.isTmrw)
                prepTmrwData(
                    allRepeatings = DI.repeatings,
                    allEvents = DI.events,
                ) else null,
            addFormInputTextValue = "",
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        TaskModel.getAscFlow()
            .onEachExIn(scope) { list ->
                state.update { it.copy(tasksUI = list.toUiList()) }
            }
        // To update daytime badges
        scope.launch {
            while (true) {
                delayToNextMinute()
                state.update { it.copy(tasksUI = TaskModel.getAsc().toUiList()) }
            }
        }
    }

    fun setAddFormInputTextValue(text: String) = state.update {
        it.copy(addFormInputTextValue = text)
    }

    fun isAddFormInputEmpty() = state.value.addFormInputTextValue.isBlank()

    fun addTask(
        onSuccess: () -> Unit,
    ) = scopeVM().launchEx {
        try {
            TaskModel.addWithValidation(state.value.addFormInputTextValue, folder)
            setAddFormInputTextValue("")
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }

    private fun List<TaskModel>.toUiList() = this
        .filter { it.folder_id == folder.id }
        .sortedByFolder(folder)
        .map { TaskUI(it) }

    ///
    ///

    class TmrwData(
        val tasksUI: List<TmrwTaskUI>,
        val curTimeString: String,
    )

    class TmrwTaskUI(
        val task: TaskModel
    ) {

        val textFeatures = task.text.textFeatures()
        val text = textFeatures.textUi()

        val timeUI = textFeatures.timeData?.let { timeData ->
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

    class TaskUI(
        val task: TaskModel,
    ) {

        val textFeatures = task.text.textFeatures()
        val text = textFeatures.textUi(withPausedEmoji = true)
        val timeUI: TimeUI? = textFeatures.timeData?.let { timeData ->
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
        val timerContext = ActivityTimerSheetVM.TimerContext.Task(task)

        fun upFolder(newFolder: TaskFolderModel) {
            launchExDefault {
                task.upFolder(newFolder, replaceIfTmrw = true)
            }
        }

        // - By manual removing;
        // - After adding to calendar;
        // - By starting from activity sheet;
        fun delete() {
            launchExDefault {
                task.delete()
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
): TasksListVM.TmrwData {

    val rawTasks = mutableListOf<TaskModel>()
    val unixTmrwDS = UnixTime(utcOffset = localUtcOffsetWithDayStart).inDays(1)
    val tmrwDSDay = unixTmrwDS.localDay
    var lastFakeTaskId = unixTmrwDS.localDayStartTime()

    // Repeatings
    allRepeatings
        .filter { it.getNextDay() == tmrwDSDay }
        .forEach { repeating ->
            rawTasks.add(
                TaskModel(
                    id = ++lastFakeTaskId,
                    text = repeating.prepTextForTask(tmrwDSDay),
                    folder_id = TaskFolderModel.ID_TODAY,
                )
            )
        }

    // Events
    allEvents
        .filter { it.getLocalTime().localDay == tmrwDSDay }
        .forEach { event ->
            rawTasks.add(
                TaskModel(
                    id = ++lastFakeTaskId,
                    text = event.prepTextForTask(),
                    folder_id = TaskFolderModel.ID_TODAY,
                )
            )
        }

    val resTasks = rawTasks
        .sortedByFolder(DI.getTodayFolder())
        .map { TasksListVM.TmrwTaskUI(it) }

    val curTimeString = unixTmrwDS.getStringByComponents(
        UnixTime.StringComponent.dayOfMonth,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.month,
        UnixTime.StringComponent.comma,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.dayOfWeek,
    )

    return TasksListVM.TmrwData(
        tasksUI = resTasks,
        curTimeString = "Tomorrow, $curTimeString"
    )
}
