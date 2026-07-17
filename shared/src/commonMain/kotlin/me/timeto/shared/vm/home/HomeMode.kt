package me.timeto.shared.vm.home

import me.timeto.shared.Cache
import me.timeto.shared.DayStartOffsetUtils
import me.timeto.shared.TaskFolderUi
import me.timeto.shared.TaskUi
import me.timeto.shared.TextFeatures
import me.timeto.shared.UnixTime
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.db.EventDb
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.textFeatures
import me.timeto.shared.vm.home.tasks.HomeTasksItemUi
import me.timeto.shared.vm.notes.NoteFormLogic
import me.timeto.shared.vm.task_form.TaskFormStrategy

sealed class HomeMode {

    data class TaskFolder(
        val taskFolderDb: TaskFolderDb,
        val allTasksUi: List<TaskUi>,
        val allRepeatingsDb: List<RepeatingDb>,
        val allEventsDb: List<EventDb>,
        val allTaskFoldersUi: List<TaskFolderUi>,
    ) : HomeMode() {

        val addTaskActivityDb: ActivityDb =
            taskFolderDb.selectActivityDbOrNullCached() ?: Cache.activitiesDb.first { it.isOther }

        val addTaskStrategy = TaskFormStrategy.NewTask(
            activityDb = addTaskActivityDb,
            taskFolderDb = taskFolderDb,
        )

        val homeTasksItemsUi: List<HomeTasksItemUi> = run {
            val listItemsUi = mutableListOf<HomeTasksItemUi>()

            val taskFolderActivityId: Int? =
                taskFolderDb.activity_id
            val tasksUi: List<HomeTasksItemUi.HomeTaskUi> = allTasksUi
                .filter {
                    (it.taskDb.folder_id == taskFolderDb.id) ||
                            (taskFolderActivityId != null && taskFolderActivityId == it.tf.activityDb?.id)
                }
                .map {
                    HomeTasksItemUi.HomeTaskUi(
                        taskUi = it,
                        allTaskFoldersUi = allTaskFoldersUi,
                    )
                }
                .sortedWith { item1, item2 ->
                    val timeData1: TextFeatures.TimeData? =
                        item1.timeUi?.timeData
                    val timeData2: TextFeatures.TimeData? =
                        item2.timeUi?.timeData
                    when {
                        timeData1 != null && timeData2 != null ->
                            if (timeData1.unixTime.time < timeData2.unixTime.time) -1 else 1
                        timeData1 != null ->
                            -1
                        timeData2 != null ->
                            1
                        else ->
                            if (item1.taskUi.taskDb.id < item2.taskUi.taskDb.id) 1 else -1
                    }
                }
            listItemsUi.addAll(tasksUi)

            if (taskFolderDb.isTomorrow) {
                listItemsUi.addAll(
                    buildTomorrowItemsUi(
                        allRepeatingsDb = allRepeatingsDb,
                        allEventsDb = allEventsDb,
                    )
                )
            }

            return@run listItemsUi
        }

    }

    data class NoteFolder(
        val noteFolderDb: NoteFolderDb,
    ) : HomeMode() {

        val addNoteLogic = NoteFormLogic.NewNote(
            noteFolderDb = noteFolderDb,
        )
    }
}

private fun buildTomorrowItemsUi(
    allRepeatingsDb: List<RepeatingDb>,
    allEventsDb: List<EventDb>,
): List<HomeTasksItemUi.HomeTomorrowItemUi> {

    val itemsUi: MutableList<HomeTasksItemUi.HomeTomorrowItemUi> =
        mutableListOf()
    val unixTimeDs: UnixTime =
        UnixTime(utcOffset = DayStartOffsetUtils.getLocalUtcOffsetCached()).inDays(1)
    val unixDayDs: Int =
        unixTimeDs.localDay
    var lastFakeTaskId: Int =
        unixTimeDs.localDayStartTime()

    // Repeatings
    allRepeatingsDb
        .filter { it.getNextDay() == unixDayDs }
        .forEach { repeatingDb ->
            itemsUi.add(
                HomeTasksItemUi.HomeTomorrowItemUi(
                    tf = repeatingDb.prepTextForTask(unixDayDs).textFeatures(),
                    type = HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.repeating,
                    listId = ++lastFakeTaskId,
                )
            )
        }

    // Calendar
    allEventsDb
        .filter { it.getLocalTime().localDay == unixDayDs }
        .forEach { eventDb ->
            itemsUi.add(
                HomeTasksItemUi.HomeTomorrowItemUi(
                    tf = eventDb.prepTextForTask().textFeatures(),
                    type = HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.calendar,
                    listId = ++lastFakeTaskId,
                )
            )
        }

    return itemsUi.sortedWith { item1, item2 ->
        val timeData1: TextFeatures.TimeData? =
            item1.timeUi?.timeData
        val timeData2: TextFeatures.TimeData? =
            item2.timeUi?.timeData
        when {
            timeData1 != null && timeData2 != null ->
                if (timeData1.unixTime.time < timeData2.unixTime.time) -1 else 1
            timeData1 != null ->
                -1
            timeData2 != null ->
                1
            else ->
                if (item1.listId < item2.listId) -1 else 1
        }
    }
}
