package me.timeto.shared

import kotlinx.coroutines.flow.filter
import me.timeto.shared.db.*

object Cache {

    var checklistsDb = listOf<ChecklistDb>()
    var checklistItemsDb = listOf<ChecklistItemDb>()
    var shortcutsDb = listOf<ShortcutDb>()
    var notesDb = listOf<NoteDb>()
    var noteFoldersDb = listOf<NoteFolderDb>()
    var kvDb = listOf<KvDb>()
    var tasksDb = listOf<TaskDb>()
    var taskFoldersDbSorted = listOf<TaskFolderDb>()
    var eventsDb = listOf<EventDb>()
    var eventTemplatesDbSorted = listOf<EventTemplateDb>()
    var repeatingsDb = listOf<RepeatingDb>()
    var activitiesDb = listOf<ActivityDb>()

    lateinit var firstIntervalDb: IntervalDb
    lateinit var lastIntervalDb: IntervalDb

    //
    // Late Init

    fun isLateInitInitialized(): Boolean =
        ::firstIntervalDb.isInitialized && ::lastIntervalDb.isInitialized

    fun fillLateInit(firstInterval: IntervalDb, lastInterval: IntervalDb) {
        this.firstIntervalDb = firstInterval
        this.lastIntervalDb = lastInterval
    }

    ///

    lateinit var todayTaskFolderDb: TaskFolderDb
    lateinit var tomorrowTaskFolderDb: TaskFolderDb
    lateinit var somedayTaskFolderDb: TaskFolderDb

    ///

    internal suspend fun init() {

        val scope = ioScope()

        //
        // Database Lists

        checklistsDb = ChecklistDb.selectAsc()
        ChecklistDb.selectAscFlow().onEachExIn(scope) { checklistsDb = it }

        checklistItemsDb = ChecklistItemDb.selectSorted()
        ChecklistItemDb.selectSortedFlow().onEachExIn(scope) { checklistItemsDb = it }

        shortcutsDb = ShortcutDb.selectAsc()
        ShortcutDb.selectAscFlow().onEachExIn(scope) { shortcutsDb = it }

        notesDb = NoteDb.selectAllSorted()
        NoteDb.selectAllSortedFlow().onEachExIn(scope) { notesDb = it }

        noteFoldersDb = NoteFolderDb.selectAllSorted()
        NoteFolderDb.selectAllSortedFlow().onEachExIn(scope) { noteFoldersDb = it }

        kvDb = KvDb.selectAll()
        KvDb.selectAllFlow().onEachExIn(scope) { kvDb = it }

        tasksDb = TaskDb.selectAsc()
        TaskDb.selectAscFlow().onEachExIn(scope) { tasksDb = it }

        val taskFoldersDbSortedLocal = TaskFolderDb.selectAllSorted()
        taskFoldersDbSorted = taskFoldersDbSortedLocal
        taskFoldersDbSortedLocal.firstOrNull { it.isToday }?.let { todayTaskFolderDb = it }
        taskFoldersDbSortedLocal.firstOrNull { it.isTomorrow }?.let { tomorrowTaskFolderDb = it }
        taskFoldersDbSortedLocal.firstOrNull { it.isSomeday }?.let { somedayTaskFolderDb = it }
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scope) { taskFoldersDbSorted_ ->
            taskFoldersDbSorted = taskFoldersDbSorted_
            taskFoldersDbSorted_.firstOrNull { it.isToday }?.let { todayTaskFolderDb = it }
            taskFoldersDbSorted_.firstOrNull { it.isTomorrow }?.let { tomorrowTaskFolderDb = it }
            taskFoldersDbSorted_.firstOrNull { it.isSomeday }?.let { somedayTaskFolderDb = it }
        }

        eventsDb = EventDb.selectAscByTime()
        EventDb.selectAscByTimeFlow().onEachExIn(scope) { eventsDb = it }

        eventTemplatesDbSorted = EventTemplateDb.selectAscSorted()
        EventTemplateDb.selectAscSortedFlow().onEachExIn(scope) { eventTemplatesDbSorted = it }

        repeatingsDb = RepeatingDb.selectAsc()
        RepeatingDb.selectAscFlow().onEachExIn(scope) { repeatingsDb = it }

        activitiesDb = ActivityDb.selectAll()
        ActivityDb.selectAllFlow().onEachExIn(scope) { activitiesDb = it }

        //
        // Late Init

        IntervalDb.selectAsc(limit = 1).firstOrNull()?.let { firstIntervalDb = it }
        IntervalDb.selectAscFlow(limit = 1).filter { it.isNotEmpty() }
            .onEachExIn(scope) { firstIntervalDb = it.first() }

        IntervalDb.selectDesc(limit = 1).firstOrNull()?.let { lastIntervalDb = it }
        IntervalDb.selectDescFlow(limit = 1).filter { it.isNotEmpty() }
            .onEachExIn(scope) { lastIntervalDb = it.first() }
    }
}
