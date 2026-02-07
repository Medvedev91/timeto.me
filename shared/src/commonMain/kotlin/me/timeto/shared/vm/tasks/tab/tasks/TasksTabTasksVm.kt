package me.timeto.shared.vm.tasks.tab.tasks

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.DaytimeUi
import me.timeto.shared.TextFeatures
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.launchExIo
import me.timeto.shared.ColorEnum
import me.timeto.shared.DayStartOffsetUtils
import me.timeto.shared.TaskUi
import me.timeto.shared.TimeFlows
import me.timeto.shared.sortedUi
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.Vm

class TasksTabTasksVm(
    val taskFolderDb: TaskFolderDb,
) : Vm<TasksTabTasksVm.State>() {

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
        combine(
            TaskDb.selectAscFlow(),
            TimeFlows.eachMinuteSecondsFlow, // Update daytime badges
        ) { tasksDb, _ ->
            state.update { it.copy(tasksVmUi = tasksDb.toUiList(taskFolderDb)) }
        }.launchIn(scopeVm)
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
            val textColorEnum: ColorEnum = if (timeData.type.isEvent())
                ColorEnum.blue else ColorEnum.secondaryText
            TmrwTimeUi(
                text = text,
                textColorEnum = textColorEnum,
            )
        }

        class TmrwTimeUi(
            val text: String,
            val textColorEnum: ColorEnum,
        )
    }

    class TaskVmUi(
        val taskUi: TaskUi,
    ) {
        val tf: TextFeatures = taskUi.tf

        val text: String = tf.textUi(withPausedEmoji = true)
        val timeUi: TimeUi? = tf.calcTimeData()?.let { timeData ->
            val unixTime = timeData.unixTime
            val isHighlight = timeData.type.isEvent() || tf.isImportant

            val timeLeftText = timeData.timeLeftText()
            val textColorEnum: ColorEnum = when (timeData.status) {
                TextFeatures.TimeData.STATUS.IN -> ColorEnum.secondaryText
                TextFeatures.TimeData.STATUS.SOON -> ColorEnum.blue
                TextFeatures.TimeData.STATUS.OVERDUE -> ColorEnum.red
            }

            if (isHighlight) {
                val backgroundColorEnum: ColorEnum =
                    if (timeData.status.isOverdue()) ColorEnum.red else ColorEnum.blue
                return@let TimeUi.HighlightUi(
                    timeData = timeData,
                    title = timeData.timeText(),
                    backgroundColorEnum = backgroundColorEnum,
                    timeLeftText = timeLeftText,
                    timeLeftColorEnum = textColorEnum,
                )
            }

            val daytimeText: String =
                DaytimeUi.byDaytime(unixTime.time - unixTime.localDayStartTime()).text
            TimeUi.RegularUi(
                timeData = timeData,
                text = "$daytimeText  $timeLeftText",
                textColorEnum = textColorEnum,
            )
        }

        fun upFolder(newFolder: TaskFolderDb) {
            launchExIo {
                taskUi.taskDb.updateFolder(newFolder, replaceIfTmrw = true)
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
                val backgroundColorEnum: ColorEnum,
                val timeLeftText: String,
                val timeLeftColorEnum: ColorEnum,
            ) : TimeUi(timeData)

            class RegularUi(
                timeData: TextFeatures.TimeData,
                val text: String,
                val textColorEnum: ColorEnum,
            ) : TimeUi(timeData)
        }
    }
}

private fun prepTmrwUi(
    allRepeatingsDb: List<RepeatingDb>,
    allEventsDb: List<EventDb>,
): TasksTabTasksVm.TmrwUi {

    val tasksDb = mutableListOf<TaskDb>()
    val unixTmrwDS = UnixTime(utcOffset = DayStartOffsetUtils.getLocalUtcOffsetCached()).inDays(1)
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
