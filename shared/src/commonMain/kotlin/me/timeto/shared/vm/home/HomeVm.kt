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
import kotlin.math.absoluteValue

class HomeVm : Vm<HomeVm.State>() {

    data class State(
        val intervalDbAndGoalDb: IntervalDbAndGoalDb,
        val isPurple: Boolean,
        val todayTasksUi: List<TaskUi>,
        val privacyMessage: String?,
        val showReadme: Boolean,
        val showRate: Boolean,
        val whatsNewMessage: String?,
        val listsContainerSize: ListsContainerSize?,
        val notificationsPermissionUi: NotificationsPermissionUi?,
        val donationsMessage: String?,
        val idToUpdate: Long,
    ) {

        val intervalDb: IntervalDb = intervalDbAndGoalDb.intervalDb
        val goalDb: Goal2Db = intervalDbAndGoalDb.goalDb

        val readmeTitle = "Goals is the main feature of this app."
        val readmeButtonText = "Read How to Use the App"

        val rateLine1 = "Hi,"
        val rateLine2 = "I try to build the best productivity app possible and would love to read your review."
        val rateNoThanks = "No Thanks"

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
            privacyMessage = null, // todo init data
            showReadme = false, // todo init data
            showRate = false, // todo init data
            whatsNewMessage = null, // todo init data
            listsContainerSize = null,
            notificationsPermissionUi = null, // todo init data
            donationsMessage = null, // todo init data
            idToUpdate = 0,
        )
    )

    init {
        val scopeVm = scopeVm()

        combine(
            IntervalDb.selectFirstOneOrNullFlow().filterNotNull(),
            IntervalDb.selectLastOneOrNullFlow().filterNotNull(),
            Goal2Db.selectAllFlow(),
            KvDb.KEY.RATE_TIME.selectIntOrNullFlow(),
            NotificationsPermission.flow,
        ) { firstIntervalDb,
            lastIntervalDb,
            goalsDb,
            rateTime,
            notificationsPermission ->

            val showRate: Boolean = run {
                val twoWeeks = 86_400 * 14
                // After 2 week since install
                if ((firstIntervalDb.id + twoWeeks) > time())
                    return@run false
                if (rateTime == null)
                    return@run true
                if (rateTime > 0)
                    return@run false
                // Every 2 weeks
                (rateTime.absoluteValue + twoWeeks) < time()
            }

            val notificationsPermissionUi: NotificationsPermissionUi? = when (notificationsPermission) {
                NotificationsPermission.notAsked -> NotificationsPermissionUi.NotAsked
                NotificationsPermission.denied -> NotificationsPermissionUi.Denied
                NotificationsPermission.rationale -> NotificationsPermissionUi.Rationale
                NotificationsPermission.granted -> null
                null -> null
            }

            state.update { state ->
                val isNewInterval: Boolean =
                    state.intervalDb.id != lastIntervalDb.id
                state.copy(
                    intervalDbAndGoalDb = IntervalDbAndGoalDb(
                        intervalDb = lastIntervalDb,
                        goalDb = goalsDb.first { it.id == lastIntervalDb.goal_id },
                    ),
                    isPurple = if (isNewInterval) false else state.isPurple,
                    showRate = showRate,
                    notificationsPermissionUi = notificationsPermissionUi,
                )
            }
        }.launchIn(scopeVm)

        TaskDb
            .selectAscFlow()
            .map { it.filter { task -> task.isToday } }
            .onEachExIn(scopeVm) { tasks ->
                state.update { it.copy(todayTasksUi = tasks.map { it.toUi() }) }
            }

        combine(
            KvDb.KEY.IS_SENDING_REPORTS.selectOrNullFlow(),
            TimeFlows.todayFlow, // To triggering every day
        ) { kvDb, _ ->
            val isFdroid = SystemInfo.instance.isFdroid
            val lastRequestTime: Int? = kvDb?.value?.toInt()
            val needToRequest: Boolean = when {
                lastRequestTime == null -> isFdroid
                lastRequestTime >= 0 -> false
                else -> (lastRequestTime.absoluteValue + 3_600 * 24 * 28) < time()
            }

            val privacyMessage: String? =
                if (!needToRequest) null
                else if (isFdroid) "Message for F-Droid Users"
                else "Developer's Message"

            state.update {
                it.copy(privacyMessage = privacyMessage)
            }
        }.launchIn(scopeVm)

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

        if (SystemInfo.instance.isFdroid)
            combine(
                KvDb.KEY.DONATIONS_TIME.selectIntOrNullFlow(),
                TimeFlows.todayFlow,
            ) { donationsTime, _ ->
                val donationsMessage: String? = run {
                    if ((donationsTime != null) && (donationsTime > 0))
                        return@run null
                    val twoWeeks = 3_600 * 24 * 14
                    if ((Cache.firstIntervalDb.id + twoWeeks) > time())
                        return@run null
                    if ((donationsTime != null) && ((donationsTime.absoluteValue + twoWeeks) > time()))
                        return@run null
                    return@run "Ask for Donations"
                }
                state.update { it.copy(donationsMessage = donationsMessage) }
            }.launchIn(scopeVm)

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

    // region Rate

    fun onRateStart() {
        launchExIo {
            KvDb.KEY.RATE_TIME.upsertInt(time())
        }
    }

    fun onRateCancel() {
        launchExIo {
            KvDb.KEY.RATE_TIME.upsertInt(-time())
        }
    }

    // endregion

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
                        existingChecklistDb ?: ChecklistDb.insertWithValidation(checklistName, isResetOnDayStarts = true)
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

    sealed class NotificationsPermissionUi {

        val title = "Allow notifications to show\ntimer always on display."
        val buttonText = "Allow Notifications"

        object NotAsked : NotificationsPermissionUi()
        object Rationale : NotificationsPermissionUi()
        object Denied : NotificationsPermissionUi()
    }
}
