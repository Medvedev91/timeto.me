package me.timeto.shared.widget

import me.timeto.shared.Cache
import me.timeto.shared.IntervalUi
import me.timeto.shared.NoteFolderUi
import me.timeto.shared.TaskFolderUi
import me.timeto.shared.TaskUi
import me.timeto.shared.TextFeatures
import me.timeto.shared.TimerStateUi
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.EventDb
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.db.TaskFolderDb
import me.timeto.shared.vm.home.HomeMode
import me.timeto.shared.vm.home.HomeModePrototype
import me.timeto.shared.vm.home.bar.HomeBarUi
import me.timeto.shared.vm.home.buttons.HomeButtonUi
import me.timeto.shared.vm.home.buttons.HomeButtonsVm
import me.timeto.shared.vm.home.tasks.homeTasksFoldersSorted

data class WidgetUi(
    val timerStateUi: TimerStateUi,
    val homeBarUi: HomeBarUi,
    val widgetChecklistUi: WidgetChecklistUi?,
    val homeButtonsUi: List<HomeButtonUi>,
) {

    companion object {

        suspend fun build(
            width: Float,
            rowHeight: Float,
            spacing: Float,
            homeModePrototype: HomeModePrototype,
        ): WidgetUi {

            val allTasksUi: List<TaskUi> = TaskDb.selectAsc().map { TaskUi(it) }
            val allRepeatingsDb: List<RepeatingDb> = RepeatingDb.selectAsc()
            val allEventsDb: List<EventDb> = EventDb.selectAscByTime()
            val allTaskFoldersUi: List<TaskFolderUi> = TaskFolderDb.selectAllSorted().map {
                TaskFolderUi(it, it.selectActivityDbOrNullCached())
            }
            val homeNoteFoldersUi: List<NoteFolderUi> = NoteFolderDb.selectAllSorted()
                .filter { it.onHome }
                .map { NoteFolderUi(it) }

            val lastIntervalDb = Cache.lastIntervalDb
            val timerStateUi = TimerStateUi(
                intervalUi = IntervalUi(
                    intervalDb = lastIntervalDb,
                    activityDb = lastIntervalDb.selectActivityDbCached(),
                ),
                todayTasksDb = allTasksUi.filter { it.taskDb.isToday }.map { it.taskDb },
                isPurple = false,
            )

            val homeMode: HomeMode = when (homeModePrototype) {
                is HomeModePrototype.TaskFolder -> HomeMode.TaskFolder(
                    taskFolderDb = homeModePrototype.taskFolderDb,
                    allTasksUi = allTasksUi,
                    allRepeatingsDb = allRepeatingsDb,
                    allEventsDb = allEventsDb,
                    allTaskFoldersUi = allTaskFoldersUi,
                    homeNoteFoldersUi = homeNoteFoldersUi,
                )
                is HomeModePrototype.NoteFolder -> HomeMode.NoteFolder(
                    noteFolderDb = homeModePrototype.noteFolderDb,
                )
            }

            val tfForTriggers: TextFeatures =
                timerStateUi.tfForTriggers
            val checklistDb: ChecklistDb? =
                tfForTriggers.checklistsDb.firstOrNull()
            val widgetChecklistUi: WidgetChecklistUi? =
                if (checklistDb == null) null
                else WidgetChecklistUi(
                    checklistDb = checklistDb,
                    itemsUi = ChecklistItemDb.selectSorted()
                        .filter { it.list_id == checklistDb.id }
                        .map { WidgetChecklistUi.ItemUi(it) },
                )

            return WidgetUi(
                timerStateUi = timerStateUi,
                homeBarUi = HomeBarUi(
                    homeMode = homeMode,
                    taskFoldersUi = allTaskFoldersUi.homeTasksFoldersSorted(),
                    noteFoldersUi = homeNoteFoldersUi,
                ),
                widgetChecklistUi = widgetChecklistUi,
                homeButtonsUi = HomeButtonsVm.buildButtonsUi(
                    width = width,
                    rowHeight = rowHeight,
                    spacing = spacing,
                ),
            )
        }
    }
}
