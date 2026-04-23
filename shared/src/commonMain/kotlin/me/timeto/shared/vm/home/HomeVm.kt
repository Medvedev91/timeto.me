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
import me.timeto.shared.db.KvDb.Companion.isCollapseHomeTasks
import me.timeto.shared.vm.whats_new.WhatsNewVm
import me.timeto.shared.vm.Vm
import kotlin.math.absoluteValue

class HomeVm : Vm<HomeVm.State>() {

    data class State(
        val intervalDbAndActivityDb: IntervalDbAndActivityDb,
        val isPurple: Boolean,
        val allTasksUi: List<TaskUi>,
        val privacyMessage: String?,
        val showReadme: Boolean,
        val showRate: Boolean,
        val whatsNewMessage: String?,
        val listsContainerSize: ListsContainerSize?,
        val notificationsPermissionUi: NotificationsPermissionUi?,
        val donationsMessage: String?,
        val allTaskFoldersDb: List<TaskFolderDb>,
        val isCollapseHomeTasks: Boolean,
        val onHomeActivity: Boolean,
        val idToUpdate: Long,
    ) {

        val intervalDb: IntervalDb =
            intervalDbAndActivityDb.intervalDb
        val activityDb: ActivityDb =
            intervalDbAndActivityDb.activityDb

        val readmeTitle = "Goals is the main feature of this app."
        val readmeButtonText = "Read How to Use the App"

        val rateLine1 = "Hi,"
        val rateLine2 = "I try to build the best productivity app possible and would love to read your review."
        val rateNoThanks = "No Thanks"

        val todayTasksUi: List<TaskUi> =
            allTasksUi.filter { it.taskDb.isToday }

        val activityTaskFolderDb: TaskFolderDb? =
            allTaskFoldersDb.firstOrNull { it.activity_id == activityDb.id }

        val activityFolderTasksUi: List<TaskUi> =
            if (activityTaskFolderDb == null) emptyList()
            else allTasksUi.filter { it.taskDb.folder_id == activityTaskFolderDb.id }

        val timerStateUi = TimerStateUi(
            intervalDb = intervalDb,
            todayTasksDb = todayTasksUi.map { it.taskDb },
            isPurple = isPurple,
        )

        val textFeaturesForTriggers: TextFeatures =
            ("${intervalDb.note ?: ""} ${activityDb.name}").textFeatures()

        val checklistDb: ChecklistDb? =
            textFeaturesForTriggers.checklistsDb.firstOrNull()

        val checklistHintUi: ChecklistHintUi? = run {
            if (checklistDb != null)
                return@run null
            if (activityDb.checklist_hint > 0)
                return@run null
            ChecklistHintUi(
                activityDb = activityDb,
            )
        }

        val extraTriggers = ExtraTriggers(
            checklistsDb = textFeaturesForTriggers.checklistsDb.filter {
                it.id != checklistDb?.id
            },
            shortcutsDb = textFeaturesForTriggers.shortcutsDb,
        )

        val mainListItemsUi: List<MainListItemUi> = run {
            val listItemsUi = mutableListOf<MainListItemUi>()

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
            if (activityTaskFolderDb == null || !isCollapseHomeTasks)
                listItemsUi.addAll(tasksUi.sortedUi(isToday = true).map { MainListItemUi.MainTaskUi(it) })

            if (activityTaskFolderDb != null) {
                listItemsUi.add(
                    MainListItemUi.TaskFolderBarUi(
                        taskFolderDb = activityTaskFolderDb,
                        todayTasksCount = tasksUi.size,
                        isCollapsed = isCollapseHomeTasks,
                    )
                )
                listItemsUi.addAll(
                    activityFolderTasksUi
                        .reversed()
                        .filter { !onHomeActivity || it.taskDb.onHomeActivity }
                        .sortedWith(compareBy({ !it.taskDb.onHomeActivity }, { -it.taskDb.id }))
                        .map { MainListItemUi.MainTaskUi(it) }
                )
            }

            return@run listItemsUi
        }

        val listsSizes: ListsSizes = run {
            val lc = listsContainerSize ?: return@run ListsSizes(0f, 0f)
            //
            // No one
            if (checklistDb == null && mainListItemsUi.isEmpty())
                return@run ListsSizes(0f, 0f)
            //
            // Only one
            if (checklistDb != null && mainListItemsUi.isEmpty())
                return@run ListsSizes(checklist = lc.totalHeight, mainTasks = 0f)
            if (checklistDb == null && mainListItemsUi.isNotEmpty())
                return@run ListsSizes(checklist = 0f, mainTasks = lc.totalHeight)
            //
            // Both
            checklistDb!!
            val halfHeight: Float = lc.totalHeight / 2
            val tasksFullHeight: Float = mainListItemsUi.size * lc.itemHeight
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
                activityDb.startInterval(
                    note = (intervalDb.note ?: "").textFeatures().copy(
                        timerType = TextFeatures.TimerType.Timer(seconds)
                    ).textWithFeatures(),
                )
            }
        }
    }

    override val state = MutableStateFlow(
        State(
            intervalDbAndActivityDb = run {
                val intervalDb = Cache.lastIntervalDb
                IntervalDbAndActivityDb(intervalDb, intervalDb.selectActivityDbCached())
            },
            isPurple = false,
            allTasksUi = Cache.tasksDb.map { it.toUi() },
            privacyMessage = null, // todo init data
            showReadme = false, // todo init data
            showRate = false, // todo init data
            whatsNewMessage = null, // todo init data
            listsContainerSize = null,
            notificationsPermissionUi = null, // todo init data
            donationsMessage = null, // todo init data
            allTaskFoldersDb = Cache.taskFoldersDbSorted,
            isCollapseHomeTasks = KvDb.KEY.IS_COLLAPSE_HOME_TASKS.selectOrNullCached().isCollapseHomeTasks(),
            onHomeActivity = true,
            idToUpdate = 0,
        )
    )

    init {
        val scopeVm = scopeVm()

        combine(
            IntervalDb.selectFirstOneOrNullFlow().filterNotNull(),
            IntervalDb.selectLastOneOrNullFlow().filterNotNull(),
            ActivityDb.selectAllFlow(),
            KvDb.KEY.RATE_TIME.selectIntOrNullFlow(),
            NotificationsPermission.flow,
            TaskDb.selectAscFlow(),
            TaskFolderDb.selectAllSortedFlow(),
            KvDb.KEY.IS_COLLAPSE_HOME_TASKS.selectOrNullFlow(),
        ) { firstIntervalDb,
            lastIntervalDb,
            activitiesDb,
            rateTime,
            notificationsPermission,
            allTasksDb,
            allTaskFoldersDb,
            isCollapseHomeTasksKvDb ->

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
                    intervalDbAndActivityDb = IntervalDbAndActivityDb(
                        intervalDb = lastIntervalDb,
                        activityDb = activitiesDb.first { it.id == lastIntervalDb.activityId },
                    ),
                    isPurple = if (isNewInterval) false else state.isPurple,
                    showRate = showRate,
                    notificationsPermissionUi = notificationsPermissionUi,
                    allTasksUi = allTasksDb.map { it.toUi() },
                    allTaskFoldersDb = allTaskFoldersDb,
                    isCollapseHomeTasks = isCollapseHomeTasksKvDb.isCollapseHomeTasks(),
                )
            }
        }.launchIn(scopeVm)

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
                        intervalDbAndActivityDb = IntervalDbAndActivityDb(
                            intervalDb = lastIntervalDb,
                            activityDb = lastIntervalDb.selectActivityDbCached(),
                        ),
                        idToUpdate = it.idToUpdate + 1, // Force update
                    )
                }
                delay(1_000L)
            }
        }
    }

    fun toggleOnHomeActivity() {
        state.update { it.copy(onHomeActivity = !it.onHomeActivity) }
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

    // Synced pair. Not cached activityDb!
    data class IntervalDbAndActivityDb(
        val intervalDb: IntervalDb,
        val activityDb: ActivityDb,
    )

    sealed class MainListItemUi(
        val id: String,
    ) {

        data class MainTaskUi(
            val taskUi: TaskUi,
        ) : MainListItemUi(id = "MainTaskUi_${taskUi.taskDb.id}") {

            val text: String =
                taskUi.tf.textUi()

            val timeUi: TimeUi? = taskUi.tf.calcTimeData()?.let { timeData ->
                TimeUi(
                    text = timeData.timeText(),
                    note = timeData.timeLeftText(),
                    status = timeData.status,
                )
            }

            fun toggleOnHomeActivity() {
                ioScope().launchEx {
                    taskUi.taskDb.toggleOnHomeActivity()
                }
            }

            class TimeUi(
                val text: String,
                val note: String,
                val status: TextFeatures.TimeData.STATUS,
            )
        }

        data class TaskFolderBarUi(
            val taskFolderDb: TaskFolderDb,
            val todayTasksCount: Int,
            val isCollapsed: Boolean,
        ) : MainListItemUi(id = "TaskFolderBarUi") {

            val addButtonText = "New Task"

            fun toggleCollapseToday() {
                ioScope().launchEx {
                    KvDb.KEY.IS_COLLAPSE_HOME_TASKS.upsertBoolean(!isCollapsed)
                }
            }
        }
    }

    data class ExtraTriggers(
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
    )

    data class ChecklistHintUi(
        val activityDb: ActivityDb,
    ) {

        val title: String =
            "New Checklist for ${activityDb.name.textFeatures().textNoFeatures}"

        fun hide() {
            launchExIo {
                activityDb.updateChecklistHint(time())
            }
        }

        fun create(
            dialogsManager: DialogsManager,
            onSuccess: (ChecklistDb) -> Unit,
        ) {
            launchExIo {
                try {
                    val oldGoalTf: TextFeatures =
                        activityDb.name.textFeatures()
                    val checklistName: String =
                        oldGoalTf.textNoFeatures
                    val existingChecklistDb: ChecklistDb? =
                        ChecklistDb.selectAsc().firstOrNull { it.name.textFeatures().textNoFeatures == checklistName }
                    val actualChecklistDb: ChecklistDb =
                        existingChecklistDb ?: ChecklistDb.insertWithValidation(checklistName, isResetOnDayStarts = true)
                    val newGoalTf: TextFeatures =
                        oldGoalTf.copy(checklistsDb = (oldGoalTf.checklistsDb + actualChecklistDb))
                    activityDb.updateNameWithValidation(newGoalTf.textWithFeatures())
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
