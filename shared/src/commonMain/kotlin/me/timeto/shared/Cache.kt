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
    var eventsDb = listOf<EventDb>()
    var eventTemplatesDbSorted = listOf<EventTemplateDb>()
    var repeatingsDb = listOf<RepeatingDb>()
    var goals2Db = listOf<Goal2Db>()

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

    fun getTodayFolderDb(): TaskFolderDb =
        taskFoldersDbSorted.first { it.isToday }

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

        notesDb = NoteDb.selectAsc()
        NoteDb.selectAscFlow().onEachExIn(scope) { notesDb = it }

        kvDb = KvDb.selectAll()
        KvDb.selectAllFlow().onEachExIn(scope) { kvDb = it }

        tasksDb = TaskDb.selectAsc()
        TaskDb.selectAscFlow().onEachExIn(scope) { tasksDb = it }

        taskFoldersDbSorted = TaskFolderDb.selectAllSorted()
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scope) { taskFoldersDbSorted = it }

        eventsDb = EventDb.selectAscByTime()
        EventDb.selectAscByTimeFlow().onEachExIn(scope) { eventsDb = it }

        eventTemplatesDbSorted = EventTemplateDb.selectAscSorted()
        EventTemplateDb.selectAscSortedFlow().onEachExIn(scope) { eventTemplatesDbSorted = it }

        repeatingsDb = RepeatingDb.selectAsc()
        RepeatingDb.selectAscFlow().onEachExIn(scope) { repeatingsDb = it }

        goals2Db = Goal2Db.selectAll()
        Goal2Db.selectAllFlow().onEachExIn(scope) { goals2Db = it }

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
