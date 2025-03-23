package me.timeto.shared

import kotlinx.coroutines.flow.filter
import me.timeto.shared.db.*
import me.timeto.shared.misc.ioScope

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
    var eventTemplatesDbSorted = listOf<EventTemplateDb>()
    var repeatingsDb = listOf<RepeatingDb>()
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

    fun getActivityDbByIdOrNull(id: Int): ActivityDb? =
        activitiesDbSorted.firstOrNull { it.id == id }

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

        tasksDb = TaskDb.getAsc()
        TaskDb.getAscFlow().onEachExIn(scope) { tasksDb = it }

        taskFoldersDbSorted = TaskFolderDb.selectAllSorted()
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scope) { taskFoldersDbSorted = it }

        activitiesDbSorted = ActivityDb.selectSorted()
        ActivityDb.selectSortedFlow().onEachExIn(scope) { activitiesDbSorted = it }

        eventsDb = EventDb.getAscByTime()
        EventDb.getAscByTimeFlow().onEachExIn(scope) { eventsDb = it }

        eventTemplatesDbSorted = EventTemplateDb.selectAscSorted()
        EventTemplateDb.selectAscSortedFlow().onEachExIn(scope) { eventTemplatesDbSorted = it }

        repeatingsDb = RepeatingDb.getAsc()
        RepeatingDb.getAscFlow().onEachExIn(scope) { repeatingsDb = it }

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
