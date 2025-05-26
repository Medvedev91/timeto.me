package me.timeto.shared.ui.tasks.tab.tasks

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.timeto.shared.Cache
import me.timeto.shared.ColorRgba
import me.timeto.shared.TextFeatures
import me.timeto.shared.UnixTime
import me.timeto.shared.daytimeToString
import me.timeto.shared.db.EventDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.delayToNextMinute
import me.timeto.shared.launchExIo
import me.timeto.shared.localUtcOffsetWithDayStart
import me.timeto.shared.misc.TaskUi
import me.timeto.shared.misc.sortedUi
import me.timeto.shared.onEachExIn
import me.timeto.shared.textFeatures
import me.timeto.shared.ui.activities.timer.ActivityTimerStrategy
import me.timeto.shared.vm.__Vm

class TasksTabTasksVm(
    val taskFolderDb: TaskFolderDb,
) : __Vm<TasksTabTasksVm.State>() {

    data class State(
        val tasksVmUi: List<TaskVmUi>,
        val tmrwUi: TmrwUi?,
    )

    override val state = MutableStateFlow(
        State(
            tasksVmUi = Cache.tasksDb.toUiList(taskFolderDb),
            tmrwUi = if (taskFolderDb.isTmrw)
                prepTmrwUi(
                    allRepeatingsDb = Cache.repeatingsDb,
                    allEventsDb = Cache.eventsDb,
                ) else null,
        )
    )

    init {
        val scopeVm = scopeVm()
        TaskDb.getAscFlow().onEachExIn(scopeVm) { list ->
            state.update { it.copy(tasksVmUi = list.toUiList(taskFolderDb)) }
        }
        // Update daytime badges
        scopeVm.launch {
            while (true) {
                delayToNextMinute()
                state.update { it.copy(tasksVmUi = TaskDb.getAsc().toUiList(taskFolderDb)) }
            }
        }
    }

    ///

    class TmrwUi(
        val tasksUi: List<TmrwTaskUi>,
        val curTimeString: String,
    )

    class TmrwTaskUi(
        val taskDb: TaskDb
    ) {

        val textFeatures: TextFeatures = taskDb.text.textFeatures()
        val text: String = textFeatures.textUi()

        val timeUi: TmrwTimeUi? = textFeatures.calcTimeData()?.let { timeData ->
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
            TmrwTimeUi(
                text = text,
                textColor = textColor,
            )
        }

        class TmrwTimeUi(
            val text: String,
            val textColor: ColorRgba,
        )
    }

    class TaskVmUi(
        val taskUi: TaskUi,
    ) {

        val text: String = taskUi.tf.textUi(withPausedEmoji = true)
        val timeUi: TimeUi? = taskUi.tf.calcTimeData()?.let { timeData ->
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
                return@let TimeUi.HighlightUi(
                    timeData = timeData,
                    title = timeData.timeText(),
                    backgroundColor = backgroundColor,
                    timeLeftText = timeLeftText,
                    timeLeftColor = textColor,
                )
            }

            val daytimeText = daytimeToString(unixTime.time - unixTime.localDayStartTime())
            TimeUi.RegularUi(
                timeData = timeData,
                text = "$daytimeText  $timeLeftText",
                textColor = textColor,
            )
        }

        val timerStrategy: ActivityTimerStrategy =
            ActivityTimerStrategy.Task(taskDb = taskUi.taskDb)

        fun upFolder(newFolder: TaskFolderDb) {
            launchExIo {
                taskUi.taskDb.upFolder(newFolder, replaceIfTmrw = true)
            }
        }

        // - By manual removing;
        // - After adding to calendar;
        // - By starting from activity sheet;
        fun delete() {
            launchExIo {
                taskUi.taskDb.delete()
            }
        }

        sealed class TimeUi(
            val timeData: TextFeatures.TimeData,
        ) {

            class HighlightUi(
                timeData: TextFeatures.TimeData,
                val title: String,
                val backgroundColor: ColorRgba,
                val timeLeftText: String,
                val timeLeftColor: ColorRgba,
            ) : TimeUi(timeData)

            class RegularUi(
                timeData: TextFeatures.TimeData,
                val text: String,
                val textColor: ColorRgba,
            ) : TimeUi(timeData)
        }
    }
}

private fun prepTmrwUi(
    allRepeatingsDb: List<RepeatingDb>,
    allEventsDb: List<EventDb>,
): TasksTabTasksVm.TmrwUi {

    val tasksDb = mutableListOf<TaskDb>()
    val unixTmrwDS = UnixTime(utcOffset = localUtcOffsetWithDayStart).inDays(1)
    val tmrwDSDay = unixTmrwDS.localDay
    var lastFakeTaskId = unixTmrwDS.localDayStartTime()

    // Repeatings
    allRepeatingsDb
        .filter { it.getNextDay() == tmrwDSDay }
        .forEach { repeatingDb ->
            tasksDb.add(
                TaskDb(
                    id = ++lastFakeTaskId,
                    text = repeatingDb.prepTextForTask(tmrwDSDay),
                    folder_id = TaskFolderDb.ID_TODAY,
                )
            )
        }

    // Events
    allEventsDb
        .filter { it.getLocalTime().localDay == tmrwDSDay }
        .forEach { eventDb ->
            tasksDb.add(
                TaskDb(
                    id = ++lastFakeTaskId,
                    text = eventDb.prepTextForTask(),
                    folder_id = TaskFolderDb.ID_TODAY,
                )
            )
        }

    val resTasks: List<TasksTabTasksVm.TmrwTaskUi> = tasksDb
        .map { TaskUi(it) }
        .sortedUi(isToday = true)
        .map { TasksTabTasksVm.TmrwTaskUi(it.taskDb) }

    val curTimeString: String = unixTmrwDS.getStringByComponents(
        UnixTime.StringComponent.dayOfMonth,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.month,
        UnixTime.StringComponent.comma,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.dayOfWeek,
    )

    return TasksTabTasksVm.TmrwUi(
        tasksUi = resTasks,
        curTimeString = "Tomorrow, $curTimeString",
    )
}

private fun List<TaskDb>.toUiList(
    taskFolderDb: TaskFolderDb,
): List<TasksTabTasksVm.TaskVmUi> = this
    .filter { it.folder_id == taskFolderDb.id }
    .map { TaskUi(it) }
    .sortedUi(isToday = taskFolderDb.isToday)
    .map { TasksTabTasksVm.TaskVmUi(it) }
