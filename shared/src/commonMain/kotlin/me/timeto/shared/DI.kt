package me.timeto.shared

import kotlinx.coroutines.flow.filter
import me.timeto.shared.db.*

object DI {

    var checklists = listOf<ChecklistModel>()
    var checklistItems = listOf<ChecklistItemModel>()
    var shortcuts = listOf<ShortcutModel>()
    var notes = listOf<NoteModel>()
    var kv = listOf<KVModel>()
    var tasks = listOf<TaskModel>()
    var taskFolders = listOf<TaskFolderModel>()
    var activitiesSorted = listOf<ActivityModel>()
    var events = listOf<EventModel>()
    var repeatings = listOf<RepeatingModel>()

    lateinit var firstInterval: IntervalModel
    lateinit var lastInterval: IntervalModel

    //
    // Late Init

    fun isLateInitInitialized() = ::firstInterval.isInitialized && ::lastInterval.isInitialized

    fun fillLateInit(firstInterval: IntervalModel, lastInterval: IntervalModel) {
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

        checklists = ChecklistModel.getAsc()
        ChecklistModel.getAscFlow().onEachExIn(scope) { checklists = it }

        checklistItems = ChecklistItemModel.getAsc()
        ChecklistItemModel.getAscFlow().onEachExIn(scope) { checklistItems = it }

        shortcuts = ShortcutModel.getAsc()
        ShortcutModel.getAscFlow().onEachExIn(scope) { shortcuts = it }

        notes = NoteModel.getAsc()
        NoteModel.getAscFlow().onEachExIn(scope) { notes = it }

        kv = KVModel.getAll()
        KVModel.getAllFlow().onEachExIn(scope) { kv = it }

        tasks = TaskModel.getAsc()
        TaskModel.getAscFlow().onEachExIn(scope) { tasks = it }

        taskFolders = TaskFolderModel.getAscBySort()
        TaskFolderModel.getAscBySortFlow().onEachExIn(scope) { taskFolders = it }

        activitiesSorted = ActivityModel.getAscSorted()
        ActivityModel.getAscSortedFlow().onEachExIn(scope) { activitiesSorted = it }

        events = EventModel.getAscByTime()
        EventModel.getAscByTimeFlow().onEachExIn(scope) { events = it }

        repeatings = RepeatingModel.getAsc()
        RepeatingModel.getAscFlow().onEachExIn(scope) { repeatings = it }

        //
        // Late Init

        IntervalModel.getAsc(limit = 1).firstOrNull()?.let { firstInterval = it }
        IntervalModel.getAscFlow(limit = 1).filter { it.isNotEmpty() }
            .onEachExIn(scope) { firstInterval = it.first() }

        IntervalModel.getDesc(limit = 1).firstOrNull()?.let { lastInterval = it }
        IntervalModel.getDescFlow(limit = 1).filter { it.isNotEmpty() }
            .onEachExIn(scope) { lastInterval = it.first() }
    }
}
