package me.timeto.shared

import kotlinx.coroutines.flow.filter
import me.timeto.shared.db.*

object DI {

    var checklists = listOf<ChecklistDb>()
    var checklistItems = listOf<ChecklistItemDb>()
    var shortcuts = listOf<ShortcutModel>()
    var notes = listOf<NoteDb>()
    var kv = listOf<KvDb>()
    var tasks = listOf<TaskModel>()
    var taskFolders = listOf<TaskFolderModel>()
    var activitiesSorted = listOf<ActivityDb>()
    var events = listOf<EventDb>()
    var eventTemplatesSorted = listOf<EventTemplateDb>()
    var repeatings = listOf<RepeatingDb>()

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

    fun getTodayFolder(): TaskFolderModel = taskFolders.first { it.isToday }

    fun getTmrwFolderOrNull(): TaskFolderModel? = taskFolders.firstOrNull { it.isTmrw }

    //
    // Mics

    fun getActivityByIdOrNull(id: Int) = activitiesSorted.firstOrNull { id == it.id }

    fun getChecklistByIdOrNull(id: Int) = checklists.firstOrNull { id == it.id }

    fun getShortcutByIdOrNull(id: Int) = shortcuts.firstOrNull { id == it.id }

    //
    // Init

    internal suspend fun init() {

        val scope = defaultScope()

        //
        // Database Lists

        checklists = ChecklistDb.getAsc()
        ChecklistDb.getAscFlow().onEachExIn(scope) { checklists = it }

        checklistItems = ChecklistItemDb.getAsc()
        ChecklistItemDb.getAscFlow().onEachExIn(scope) { checklistItems = it }

        shortcuts = ShortcutModel.getAsc()
        ShortcutModel.getAscFlow().onEachExIn(scope) { shortcuts = it }

        notes = NoteDb.getAsc()
        NoteDb.getAscFlow().onEachExIn(scope) { notes = it }

        kv = KvDb.getAll()
        KvDb.getAllFlow().onEachExIn(scope) { kv = it }

        tasks = TaskModel.getAsc()
        TaskModel.getAscFlow().onEachExIn(scope) { tasks = it }

        taskFolders = TaskFolderModel.getAscBySort()
        TaskFolderModel.getAscBySortFlow().onEachExIn(scope) { taskFolders = it }

        activitiesSorted = ActivityDb.getAscSorted()
        ActivityDb.getAscSortedFlow().onEachExIn(scope) { activitiesSorted = it }

        events = EventDb.getAscByTime()
        EventDb.getAscByTimeFlow().onEachExIn(scope) { events = it }

        eventTemplatesSorted = EventTemplateDb.selectAscSorted()
        EventTemplateDb.selectAscSortedFlow().onEachExIn(scope) { eventTemplatesSorted = it }

        repeatings = RepeatingDb.getAsc()
        RepeatingDb.getAscFlow().onEachExIn(scope) { repeatings = it }

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
