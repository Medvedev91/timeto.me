package me.timeto.shared

import kotlinx.coroutines.flow.filter
import me.timeto.shared.db.*

object Cache {

    var checklistsDb = listOf<ChecklistDb>()
    var checklistItemsDb = listOf<ChecklistItemDb>()
    var shortcutsDb = listOf<ShortcutDb>()
    var notesDb = listOf<NoteDb>()
    var kvDb = listOf<KvDb>()
    var tasksDb = listOf<TaskDb>()
    var taskFoldersDbSorted = listOf<TaskFolderDb>()
    var activitiesDbSorted = listOf<ActivityDb>()
    var eventsDb = listOf<EventDb>()
    var eventTemplatesSorted = listOf<EventTemplateDb>()
    var repeatings = listOf<RepeatingDb>()
    var goalsDb = listOf<GoalDb>()

    lateinit var firstInterval: IntervalDb
    lateinit var lastInterval: IntervalDb

    //
    // Late Init

    fun isLateInitInitialized(): Boolean =
        ::firstInterval.isInitialized && ::lastInterval.isInitialized

    fun fillLateInit(firstInterval: IntervalDb, lastInterval: IntervalDb) {
        this.firstInterval = firstInterval
        this.lastInterval = lastInterval
    }

    ///

    fun getTodayFolderDb(): TaskFolderDb =
        taskFoldersDbSorted.first { it.isToday }

    ///

    internal suspend fun init() {

        val scope = ioScope()

        //
        // Database Lists

        checklistsDb = ChecklistDb.getAsc()
        ChecklistDb.getAscFlow().onEachExIn(scope) { checklistsDb = it }

        checklistItemsDb = ChecklistItemDb.getSorted()
        ChecklistItemDb.getSortedFlow().onEachExIn(scope) { checklistItemsDb = it }

        shortcutsDb = ShortcutDb.getAsc()
        ShortcutDb.getAscFlow().onEachExIn(scope) { shortcutsDb = it }

        notesDb = NoteDb.getAsc()
        NoteDb.getAscFlow().onEachExIn(scope) { notesDb = it }

        kvDb = KvDb.selectAll()
        KvDb.selectAllFlow().onEachExIn(scope) { kvDb = it }

        tasksDb = TaskDb.getAsc()
        TaskDb.getAscFlow().onEachExIn(scope) { tasksDb = it }

        taskFoldersDbSorted = TaskFolderDb.selectAllSorted()
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scope) { taskFoldersDbSorted = it }

        activitiesDbSorted = ActivityDb.getAscSorted()
        ActivityDb.getAscSortedFlow().onEachExIn(scope) { activitiesDbSorted = it }

        eventsDb = EventDb.getAscByTime()
        EventDb.getAscByTimeFlow().onEachExIn(scope) { eventsDb = it }

        eventTemplatesSorted = EventTemplateDb.selectAscSorted()
        EventTemplateDb.selectAscSortedFlow().onEachExIn(scope) { eventTemplatesSorted = it }

        repeatings = RepeatingDb.getAsc()
        RepeatingDb.getAscFlow().onEachExIn(scope) { repeatings = it }

        goalsDb = GoalDb.selectAll()
        GoalDb.selectAllFlow().onEachExIn(scope) { goalsDb = it }

        //
        // Late Init

        IntervalDb.getAsc(limit = 1).firstOrNull()?.let { firstInterval = it }
        IntervalDb.getAscFlow(limit = 1).filter { it.isNotEmpty() }
            .onEachExIn(scope) { firstInterval = it.first() }

        IntervalDb.getDesc(limit = 1).firstOrNull()?.let { lastInterval = it }
        IntervalDb.getDescFlow(limit = 1).filter { it.isNotEmpty() }
            .onEachExIn(scope) { lastInterval = it.first() }
    }
}
