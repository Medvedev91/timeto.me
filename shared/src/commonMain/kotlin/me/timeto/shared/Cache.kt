package me.timeto.shared

import kotlinx.coroutines.flow.filter
import me.timeto.shared.db.*

object Cache {

    var checklists = listOf<ChecklistDb>()
    var checklistItems = listOf<ChecklistItemDb>()
    var shortcuts = listOf<ShortcutDb>()
    var notes = listOf<NoteDb>()
    var kv = listOf<KvDb>()
    var tasks = listOf<TaskDb>()
    var taskFolders = listOf<TaskFolderDb>()
    var activitiesSorted = listOf<ActivityDb>()
    var events = listOf<EventDb>()
    var eventTemplatesSorted = listOf<EventTemplateDb>()
    var repeatings = listOf<RepeatingDb>()
    var goalsDb = listOf<GoalDb>()

    lateinit var firstInterval: IntervalDb
    lateinit var lastInterval: IntervalDb

    //
    // Late Init

    fun isLateInitInitialized() = ::firstInterval.isInitialized && ::lastInterval.isInitialized

    fun fillLateInit(firstInterval: IntervalDb, lastInterval: IntervalDb) {
        this.firstInterval = firstInterval
        this.lastInterval = lastInterval
    }

    //
    // Task Folders

    fun getTodayFolder(): TaskFolderDb = taskFolders.first { it.isToday }

    //
    // Mics

    fun getActivityByIdOrNull(id: Int) = activitiesSorted.firstOrNull { id == it.id }

    fun getChecklistByIdOrNull(id: Int) = checklists.firstOrNull { id == it.id }

    fun getShortcutByIdOrNull(id: Int) = shortcuts.firstOrNull { id == it.id }

    //
    // Init

    internal suspend fun init() {

        val scope = ioScope()

        //
        // Database Lists

        checklists = ChecklistDb.getAsc()
        ChecklistDb.getAscFlow().onEachExIn(scope) { checklists = it }

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

        taskFolders = TaskFolderDb.selectAllSorted()
        TaskFolderDb.getAscBySortFlow().onEachExIn(scope) { taskFolders = it }

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
