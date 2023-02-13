package timeto.shared

import kotlinx.coroutines.flow.filter
import timeto.shared.db.*

object DI {

    var shortcuts = listOf<ShortcutModel>()
    var checklists = listOf<ChecklistModel>()
    var checklistItems = listOf<ChecklistItemModel>()
    var kv = listOf<KVModel>()
    var tasks = listOf<TaskModel>()
    var taskFolders = listOf<TaskFolderModel>()
    var activitiesSorted = listOf<ActivityModel>()
    var events = listOf<EventModel>()
    var repeatings = listOf<RepeatingModel>()
    var hotIntervalsDesc = listOf<IntervalModel>()

    lateinit var firstInterval: IntervalModel
    lateinit var lastInterval: IntervalModel

    ///
    /// Late Init

    fun isLateInitInitialized() = ::firstInterval.isInitialized && ::lastInterval.isInitialized

    fun fillLateInit(firstInterval: IntervalModel, lastInterval: IntervalModel) {
        this.firstInterval = firstInterval
        this.lastInterval = lastInterval
    }

    ///
    /// Task Folders

    fun getTodayFolder(): TaskFolderModel = taskFolders.first { it.isToday }

    fun getTmrwFolderOrNull(): TaskFolderModel? = taskFolders.firstOrNull { it.isTmrw }

    ///
    /// Init

    internal suspend fun init() {

        val scope = defaultScope()

        ///
        /// Database Lists

        shortcuts = ShortcutModel.getAsc()
        ShortcutModel.getAscFlow().onEachExIn(scope) { shortcuts = it }

        checklists = ChecklistModel.getAsc()
        ChecklistModel.getAscFlow().onEachExIn(scope) { checklists = it }

        checklistItems = ChecklistItemModel.getAsc()
        ChecklistItemModel.getAscFlow().onEachExIn(scope) { checklistItems = it }

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

        hotIntervalsDesc = IntervalModel.getDesc(IntervalModel.HOT_INTERVALS_LIMIT)
        IntervalModel.getDescFlow(limit = IntervalModel.HOT_INTERVALS_LIMIT)
            .onEachExIn(scope) { hotIntervalsDesc = it }

        ///
        /// Late Init

        IntervalModel.getAsc(limit = 1).firstOrNull()?.let { firstInterval = it }
        IntervalModel.getAscFlow(limit = 1).filter { it.isNotEmpty() }
            .onEachExIn(scope) { firstInterval = it.first() }

        IntervalModel.getDesc(limit = 1).firstOrNull()?.let { lastInterval = it }
        IntervalModel.getDescFlow(limit = 1).filter { it.isNotEmpty() }
            .onEachExIn(scope) { lastInterval = it.first() }
    }
}
