package me.timeto.shared

import kotlinx.coroutines.flow.filter
import me.timeto.shared.db.*

object Cache {

    var checklistsDb = listOf<ChecklistDb>()
    var checklistItems = listOf<ChecklistItemDb>()
    var shortcuts = listOf<ShortcutDb>()
    var notes = listOf<NoteDb>()
    var kv = listOf<KvDb>()
    var tasks = listOf<TaskDb>()
    var taskFoldersDbSorted = listOf<TaskFolderDb>()
    var activitiesSorted = listOf<ActivityDb>()
    var events = listOf<EventDb>()
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

        checklistItems = ChecklistItemDb.getSorted()
        ChecklistItemDb.getSortedFlow().onEachExIn(scope) { checklistItems = it }

        shortcuts = ShortcutDb.getAsc()
        ShortcutDb.getAscFlow().onEachExIn(scope) { shortcuts = it }

        notes = NoteDb.getAsc()
        NoteDb.getAscFlow().onEachExIn(scope) { notes = it }

        kv = KvDb.selectAll()
        KvDb.selectAllFlow().onEachExIn(scope) { kv = it }

        tasks = TaskDb.getAsc()
        TaskDb.getAscFlow().onEachExIn(scope) { tasks = it }

        taskFoldersDbSorted = TaskFolderDb.selectAllSorted()
        TaskFolderDb.selectAllSortedFlow().onEachExIn(scope) { taskFoldersDbSorted = it }

        activitiesSorted = ActivityDb.getAscSorted()
        ActivityDb.getAscSortedFlow().onEachExIn(scope) { activitiesSorted = it }

        events = EventDb.getAscByTime()
        EventDb.getAscByTimeFlow().onEachExIn(scope) { events = it }

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
