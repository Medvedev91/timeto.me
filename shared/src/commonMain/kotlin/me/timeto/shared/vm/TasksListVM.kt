package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.TextFeatures.TimeData
import me.timeto.shared.db.EventModel
import me.timeto.shared.db.RepeatingModel
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
        val timeUI: TimeUI? = textFeatures.timeData?.let {
            TimeUI.prepItem(timeData = it, isImportant = textFeatures.isImportant)
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

        sealed class TimeUI {

            companion object {

                fun prepItem(
                    timeData: TimeData,
                    isImportant: Boolean,
                ): TimeUI {

                    val timeLeftText = timeData.timeLeftText()
                    val unixTime = timeData.unixTime
                    val textColor = when (timeData.status) {
                        TimeData.STATUS.IN -> ColorRgba.textSecondary
                        TimeData.STATUS.SOON -> ColorRgba.blue
                        TimeData.STATUS.OVERDUE -> ColorRgba.red
                    }

                    val isEvent = timeData.type.isEvent()

                    if (isEvent || isImportant) {

                        val type: HighlightUI.TYPE = when {
                            isEvent -> HighlightUI.TYPE.event
                            isImportant -> HighlightUI.TYPE.important
                            else -> throw Exception("TasksListVM invalid type")
                        }

                        val fullDateComponents = listOf(
                            UnixTime.StringComponent.dayOfMonth,
                            UnixTime.StringComponent.space,
                            UnixTime.StringComponent.month3,
                            UnixTime.StringComponent.comma,
                            UnixTime.StringComponent.space,
                            UnixTime.StringComponent.hhmm24,
                        )

                        val timeComponents = when (type) {
                            HighlightUI.TYPE.event -> fullDateComponents
                            HighlightUI.TYPE.important ->
                                if (timeData.unixTime.isToday())
                                    listOf(UnixTime.StringComponent.hhmm24)
                                else fullDateComponents
                        }

                        val backgroundColor = if (timeData.status == TimeData.STATUS.OVERDUE)
                            ColorRgba.red else ColorRgba.blue

                        return HighlightUI(
                            title = timeData.unixTime.getStringByComponents(timeComponents),
                            backgroundColor = backgroundColor,
                            timeLeftText = timeLeftText,
                            timeLeftColor = textColor,
                            type = type
                        )
                    }

                    val daytimeText = daytimeToString(unixTime.time - unixTime.localDayStartTime())
                    return RegularUI(
                        text = "$daytimeText  $timeLeftText",
                        textColor = textColor,
                    )
                }
            }

            class HighlightUI(
                val title: String,
                val backgroundColor: ColorRgba,
                val timeLeftText: String,
                val timeLeftColor: ColorRgba,
                val type: TYPE,
            ) : TimeUI() {

                enum class TYPE {

                    event, important;

                    fun isEvent() = this == event
                    fun isImportant() = this == important
                }
            }

            class RegularUI(
                val text: String,
                val textColor: ColorRgba,
            ) : TimeUI()
        }
    }
}

private fun prepTmrwData(
    allRepeatings: List<RepeatingModel>,
    allEvents: List<EventModel>,
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
