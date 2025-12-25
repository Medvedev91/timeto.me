package me.timeto.shared.vm.home

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.*
import me.timeto.shared.db.KvDb.Companion.todayOnHomeScreen
import me.timeto.shared.limitMin
import me.timeto.shared.SystemInfo
import me.timeto.shared.TaskUi
import me.timeto.shared.sortedUi
import me.timeto.shared.time
import me.timeto.shared.TimerStateUi
import me.timeto.shared.vm.whats_new.WhatsNewVm
import me.timeto.shared.vm.Vm

class HomeVm : Vm<HomeVm.State>() {

    data class State(
        val intervalDbAndGoalDb: IntervalDbAndGoalDb,
        val isPurple: Boolean,
        val todayTasksUi: List<TaskUi>,
        val fdroidMessage: String?,
        val showReadme: Boolean,
        val whatsNewMessage: String?,
        val listsContainerSize: ListsContainerSize?,
        val idToUpdate: Long,
    ) {

        val intervalDb: IntervalDb = intervalDbAndGoalDb.intervalDb
        val goalDb: Goal2Db = intervalDbAndGoalDb.goalDb

        val readmeTitle = "Goals is the main feature of this app."
        val readmeButtonText = "Read How to Use the App"

        val timerStateUi = TimerStateUi(
            intervalDb = intervalDb,
            todayTasksDb = todayTasksUi.map { it.taskDb },
            isPurple = isPurple,
        )

        // todo or use interval.getTriggers()
        val textFeatures: TextFeatures =
            (intervalDb.note ?: goalDb.name).textFeatures()

        val checklistDb: ChecklistDb? =
            textFeatures.checklistsDb.firstOrNull()

        val checklistHintUi: ChecklistHintUi? = run {
            if (checklistDb != null)
                return@run null
            if (goalDb.checklist_hint > 0)
                return@run null
            ChecklistHintUi(
                goalDb = goalDb,
            )
        }

        val extraTriggers = ExtraTriggers(
            checklistsDb = textFeatures.checklistsDb.filter {
                it.id != checklistDb?.id
            },
            shortcutsDb = textFeatures.shortcutsDb,
        )

        val mainTasks: List<MainTask> = run {
            val tasksUi: List<TaskUi> =
                if (KvDb.KEY.TODAY_ON_HOME_SCREEN.selectOrNullCached().todayOnHomeScreen())
                    todayTasksUi
                else
                    todayTasksUi.filter { taskUi ->
                        val taskTf = taskUi.tf
                        // Condition
                        (taskTf.paused != null) ||
                                taskTf.isImportant ||
                                (taskTf.calcTimeData()?.type?.isEvent() == true)
                    }
            tasksUi.sortedUi(true).map { MainTask(it) }
        }

        val listsSizes: ListsSizes = run {
            val lc = listsContainerSize ?: return@run ListsSizes(0f, 0f)
            //
            // No one
            if (checklistDb == null && mainTasks.isEmpty())
                return@run ListsSizes(0f, 0f)
            //
            // Only one
            if (checklistDb != null && mainTasks.isEmpty())
                return@run ListsSizes(checklist = lc.totalHeight, mainTasks = 0f)
            if (checklistDb == null && mainTasks.isNotEmpty())
                return@run ListsSizes(checklist = 0f, mainTasks = lc.totalHeight)
            //
            // Both
            checklistDb!!
            val halfHeight: Float = lc.totalHeight / 2
            val tasksCount: Int = mainTasks.size
            val tasksFullHeight: Float = tasksCount * lc.itemHeight
            // Tasks smaller the half
            if (tasksFullHeight < halfHeight)
                return@run ListsSizes(
                    checklist = lc.totalHeight - tasksFullHeight,
                    mainTasks = tasksFullHeight,
                )
            // Tasks bigger the half
            val checklistCount: Int = checklistDb.getItemsCached().size.limitMin(1)
            val checklistFullHeight: Float = checklistCount * lc.itemHeight
            if (checklistFullHeight < halfHeight)
                return@run ListsSizes(
                    checklist = checklistFullHeight,
                    mainTasks = lc.totalHeight - checklistFullHeight,
                )
            ListsSizes(checklist = halfHeight, mainTasks = halfHeight)
        }

        fun startFromTimer(seconds: Int) {
            launchExIo {
                IntervalDb.insertWithValidation(
                    timer = seconds,
                    goalDb = goalDb,
                    note = intervalDb.note,
                )
            }
        }
    }

    override val state = MutableStateFlow(
        State(
            intervalDbAndGoalDb = run {
                val intervalDb = Cache.lastIntervalDb
                IntervalDbAndGoalDb(intervalDb, intervalDb.selectGoalDbCached())
            },
            isPurple = false,
            todayTasksUi = listOf(),
            fdroidMessage = null, // todo init data
            showReadme = false, // todo init data
            whatsNewMessage = null, // todo init data
            listsContainerSize = null,
            idToUpdate = 0,
        )
    )

    init {
        val scopeVm = scopeVm()

        combine(
            IntervalDb.selectLastOneOrNullFlow().filterNotNull(),
            Goal2Db.selectAllFlow(),
        ) { lastIntervalDb, goalsDb ->
            state.update { state ->
                val isNewInterval: Boolean =
                    state.intervalDb.id != lastIntervalDb.id
                state.copy(
                    intervalDbAndGoalDb = IntervalDbAndGoalDb(
                        intervalDb = lastIntervalDb,
                        goalDb = goalsDb.first { it.id == lastIntervalDb.goal_id },
                    ),
                    isPurple = if (isNewInterval) false else state.isPurple,
                )
            }
        }.launchIn(scopeVm)

        TaskDb
            .selectAscFlow()
            .map { it.filter { task -> task.isToday } }
            .onEachExIn(scopeVm) { tasks ->
                state.update { it.copy(todayTasksUi = tasks.map { it.toUi() }) }
            }

        if (SystemInfo.instance.isFdroid)
            KvDb.KEY.IS_SENDING_REPORTS
                .selectOrNullFlow()
                .onEachExIn(scopeVm) { kvDb ->
                    state.update {
                        it.copy(fdroidMessage = if (kvDb == null) "Message for F-Droid Users" else null)
                    }
                }

        KvDb.KEY.HOME_README_OPEN_TIME
            .selectOrNullFlow()
            .onEachExIn(scopeVm) { kvDb ->
                state.update {
                    it.copy(showReadme = kvDb == null)
                }
            }

        KvDb.KEY.WHATS_NEW_CHECK_UNIX_DAY
            .selectOrNullFlow()
            .onEachExIn(scopeVm) { kvDb ->
                val lastHistoryUnixDay: Int =
                    WhatsNewVm.historyItemsUi.first().unixDay
                val message: String? =
                    if ((kvDb == null) || (lastHistoryUnixDay > kvDb.value.toInt()))
                        "What's New"
                    else null
                state.update {
                    it.copy(whatsNewMessage = message)
                }
            }

        ///

        scopeVm.launch {
            while (true) {
                state.update {
                    val lastIntervalDb = Cache.lastIntervalDb
                    it.copy(
                        intervalDbAndGoalDb = IntervalDbAndGoalDb(
                            intervalDb = lastIntervalDb,
                            goalDb = lastIntervalDb.selectGoalDbCached(),
                        ),
                        idToUpdate = it.idToUpdate + 1, // Force update
                    )
                }
                delay(1_000L)
            }
        }
    }

    fun upListsContainerSize(
        totalHeight: Float,
        itemHeight: Float,
    ) {
        val lc = ListsContainerSize(totalHeight, itemHeight)
        if (lc == state.value.listsContainerSize)
            return
        state.update { it.copy(listsContainerSize = lc) }
    }

    fun onReadmeOpen() {
        launchExIo {
            KvDb.KEY.HOME_README_OPEN_TIME.upsertInt(time())
        }
    }

    fun toggleIsPurple() {
        state.update { it.copy(isPurple = !it.isPurple) }
    }

    ///

    data class ListsContainerSize(
        val totalHeight: Float,
        val itemHeight: Float,
    )

    data class ListsSizes(
        val checklist: Float,
        val mainTasks: Float,
    )

    ///

    // Synced pair. Not cached goalDb!
    data class IntervalDbAndGoalDb(
        val intervalDb: IntervalDb,
        val goalDb: Goal2Db,
    )

    class MainTask(
        val taskUi: TaskUi,
    ) {

        val text = taskUi.tf.textUi()

        val timeUi: TimeUi? = taskUi.tf.calcTimeData()?.let { timeData ->
            TimeUi(
                text = timeData.timeText(),
                note = timeData.timeLeftText(),
                status = timeData.status,
            )
        }

        class TimeUi(
            val text: String,
            val note: String,
            val status: TextFeatures.TimeData.STATUS,
        )
    }

    data class ExtraTriggers(
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
    )

    data class ChecklistHintUi(
        val goalDb: Goal2Db,
    ) {

        val title: String =
            "New Checklist for ${goalDb.name.textFeatures().textNoFeatures}"

        fun hide() {
            launchExIo {
                goalDb.updateChecklistHint(time())
            }
        }

        fun create(
            dialogsManager: DialogsManager,
            onSuccess: (ChecklistDb) -> Unit,
        ) {
            launchExIo {
                try {
                    val oldGoalTf: TextFeatures =
                        goalDb.name.textFeatures()
                    val checklistName: String =
                        oldGoalTf.textNoFeatures
                    val existingChecklistDb: ChecklistDb? =
                        ChecklistDb.selectAsc().firstOrNull { it.name.textFeatures().textNoFeatures == checklistName }
                    val actualChecklistDb: ChecklistDb =
                        existingChecklistDb ?: ChecklistDb.insertWithValidation(checklistName)
                    val newGoalTf: TextFeatures =
                        oldGoalTf.copy(checklistsDb = (oldGoalTf.checklistsDb + actualChecklistDb))
                    goalDb.updateNameWithValidation(newGoalTf.textWithFeatures())
                    onSuccess(actualChecklistDb)
                } catch (uiException: UiException) {
                    dialogsManager.alert(uiException.uiMessage)
                }
            }
        }
    }
}
