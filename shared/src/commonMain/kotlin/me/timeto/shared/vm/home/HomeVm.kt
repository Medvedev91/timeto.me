package me.timeto.shared.vm.home

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.timeto.shared.*
import me.timeto.shared.db.*
import me.timeto.shared.limitMin
import me.timeto.shared.SystemInfo
import me.timeto.shared.TaskUi
import me.timeto.shared.time
import me.timeto.shared.TimerStateUi
import me.timeto.shared.vm.whats_new.WhatsNewVm
import me.timeto.shared.vm.Vm
import me.timeto.shared.vm.home.tasks.HomeTasksBarUi
import me.timeto.shared.vm.home.tasks.HomeTasksItemUi
import me.timeto.shared.vm.home.tasks.homeTasksFoldersSorted
import kotlin.math.absoluteValue

class HomeVm : Vm<HomeVm.State>() {

    companion object {

        private val showStartScreenFlow = MutableSharedFlow<Unit>()

        fun showStartScreen() {
            launchExIo {
                showStartScreenFlow.emit(Unit)
            }
        }
    }

    data class State(
        val intervalDbAndActivityDb: IntervalDbAndActivityDb,
        val isPurple: Boolean,
        val allTasksUi: List<TaskUi>,
        val privacyMessage: String?,
        // todo remove after update July 2026
        val showDocBanner: Boolean,
        val forceOpenDoc: Boolean,
        val showRate: Boolean,
        val whatsNewMessage: String?,
        val listsContainerSize: ListsContainerSize?,
        val notificationsPermissionUi: NotificationsPermissionUi?,
        val donationsMessage: String?,
        val allRepeatingsDb: List<RepeatingDb>,
        val allEventsDb: List<EventDb>,
        val allTaskFoldersUi: List<TaskFolderUi>,
        val taskFolderUi: TaskFolderUi,
        val idToUpdate: Long,
    ) {

        val intervalDb: IntervalDb =
            intervalDbAndActivityDb.intervalDb
        val activityDb: ActivityDb =
            intervalDbAndActivityDb.activityDb

        // todo remove. Needed only for old users.
        val readmeTitle = "New Readme is Here!"
        val readmeButtonText = "Read How to Use the App"

        val rateLine1 = "Hi,"
        val rateLine2 = "I try to build the best productivity app possible and would love to read your review."
        val rateNoThanks = "No Thanks"

        val timerStateUi = TimerStateUi(
            intervalDb = intervalDb,
            todayTasksDb = allTasksUi.filter { it.taskDb.isToday }.map { it.taskDb },
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

        val homeTasksItemsUi: List<HomeTasksItemUi> = run {
            val listItemsUi = mutableListOf<HomeTasksItemUi>()

            val taskFolderActivityId: Int? =
                taskFolderUi.taskFolderDb.activity_id
            val tasksUi: List<HomeTasksItemUi.HomeTaskUi> = allTasksUi
                .filter {
                    (it.taskDb.folder_id == taskFolderUi.taskFolderDb.id) ||
                            (taskFolderActivityId != null && taskFolderActivityId == it.tf.activityDb?.id)
                }
                .map {
                    HomeTasksItemUi.HomeTaskUi(
                        taskUi = it,
                        allTaskFoldersUi = allTaskFoldersUi,
                    )
                }
                .sortedWith { item1, item2 ->
                    val timeData1: TextFeatures.TimeData? =
                        item1.timeUi?.timeData
                    val timeData2: TextFeatures.TimeData? =
                        item2.timeUi?.timeData
                    when {
                        timeData1 != null && timeData2 != null ->
                            if (timeData1.unixTime.time < timeData2.unixTime.time) -1 else 1
                        timeData1 != null ->
                            -1
                        timeData2 != null ->
                            1
                        else ->
                            if (item1.taskUi.taskDb.id < item2.taskUi.taskDb.id) 1 else -1
                    }
                }
            listItemsUi.addAll(tasksUi)

            if (taskFolderUi.taskFolderDb.isTomorrow) {
                listItemsUi.addAll(
                    buildTomorrowItemsUi(
                        allRepeatingsDb = allRepeatingsDb,
                        allEventsDb = allEventsDb,
                    )
                )
            }

            return@run listItemsUi
        }

        val tasksBarUi = HomeTasksBarUi(
            taskFolderDb = taskFolderUi.taskFolderDb,
            taskFoldersUi = allTaskFoldersUi.homeTasksFoldersSorted(),
        )

        val listsSizes: ListsSizes = run {
            val lc = listsContainerSize ?: return@run ListsSizes(0f, 0f)
            //
            // No one
            if (checklistDb == null && homeTasksItemsUi.isEmpty())
                return@run ListsSizes(0f, 0f)
            //
            // Only one
            if (checklistDb != null && homeTasksItemsUi.isEmpty())
                return@run ListsSizes(checklist = lc.totalHeight, mainTasks = 0f)
            if (checklistDb == null && homeTasksItemsUi.isNotEmpty())
                return@run ListsSizes(checklist = 0f, mainTasks = lc.totalHeight)
            //
            // Both
            checklistDb!!
            val halfHeight: Float = lc.totalHeight / 2
            val tasksFullHeight: Float = homeTasksItemsUi.size * lc.itemHeight
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
            showDocBanner = false, // todo init data
            forceOpenDoc = false, // todo init data
            showRate = false, // todo init data
            whatsNewMessage = null, // todo init data
            listsContainerSize = null,
            notificationsPermissionUi = null, // todo init data
            donationsMessage = null, // todo init data
            allRepeatingsDb = Cache.repeatingsDb,
            allEventsDb = Cache.eventsDb,
            allTaskFoldersUi = Cache.taskFoldersDbSorted.map {
                TaskFolderUi(it, it.selectActivityDbOrNullCached())
            },
            taskFolderUi = TaskFolderUi(
                taskFolderDb = Cache.todayTaskFolderDb,
                activityDb = null, // Always null for today
            ),
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
            RepeatingDb.selectAscFlow(),
            EventDb.selectAscByTimeFlow(),
            TaskDb.selectAscFlow(),
            TaskFolderDb.selectAllSortedFlow(),
        ) { firstIntervalDb,
            lastIntervalDb,
            activitiesDb,
            rateTime,
            notificationsPermission,
            allRepeatingsDb,
            allEventsDb,
            allTasksDb,
            allTaskFoldersDb ->

            val showRate: Boolean = run {
                val twoWeeks = 86_400 * 14
                // After 2 week since install
                if ((firstIntervalDb.time + twoWeeks) > time())
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
                    allRepeatingsDb = allRepeatingsDb,
                    allEventsDb = allEventsDb,
                    allTasksUi = allTasksDb.map { it.toUi() },
                    allTaskFoldersUi = allTaskFoldersDb.map { taskFolderDb ->
                        TaskFolderUi(
                            taskFolderDb = taskFolderDb,
                            activityDb = activitiesDb.firstOrNull { it.id == taskFolderDb.activity_id },
                        )
                    },
                )
            }
        }.launchIn(scopeVm)

        showStartScreenFlow.onEachExIn(scopeVm) {
            setTodayTaskFolder()
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

        KvDb.KEY.DOC_FORCE_READ_TIME
            .selectOrNullFlow()
            .onEachExIn(scopeVm) { kvDb ->
                // todo always show after update July 2026
                val forceOpenDoc: Boolean = when {
                    kvDb != null -> false
                    else -> (Cache.firstIntervalDb.time + 3_600 * 60) > time()
                }
                state.update {
                    it.copy(
                        showDocBanner = kvDb == null,
                        forceOpenDoc = forceOpenDoc,
                    )
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
                    if ((Cache.firstIntervalDb.time + twoWeeks) > time())
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

    fun updateTaskFolder(taskFolderUi: TaskFolderUi) {
        state.update { it.copy(taskFolderUi = taskFolderUi) }
    }

    fun setTodayTaskFolder() {
        updateTaskFolder(
            TaskFolderUi(
                taskFolderDb = Cache.todayTaskFolderDb,
                activityDb = null, // Always null for today
            )
        )
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

private fun buildTomorrowItemsUi(
    allRepeatingsDb: List<RepeatingDb>,
    allEventsDb: List<EventDb>,
): List<HomeTasksItemUi.HomeTomorrowItemUi> {

    val itemsUi: MutableList<HomeTasksItemUi.HomeTomorrowItemUi> =
        mutableListOf()
    val unixTimeDs: UnixTime =
        UnixTime(utcOffset = DayStartOffsetUtils.getLocalUtcOffsetCached()).inDays(1)
    val unixDayDs: Int =
        unixTimeDs.localDay
    var lastFakeTaskId: Int =
        unixTimeDs.localDayStartTime()

    // Repeatings
    allRepeatingsDb
        .filter { it.getNextDay() == unixDayDs }
        .forEach { repeatingDb ->
            itemsUi.add(
                HomeTasksItemUi.HomeTomorrowItemUi(
                    tf = repeatingDb.prepTextForTask(unixDayDs).textFeatures(),
                    type = HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.repeating,
                    listId = ++lastFakeTaskId,
                )
            )
        }

    // Calendar
    allEventsDb
        .filter { it.getLocalTime().localDay == unixDayDs }
        .forEach { eventDb ->
            itemsUi.add(
                HomeTasksItemUi.HomeTomorrowItemUi(
                    tf = eventDb.prepTextForTask().textFeatures(),
                    type = HomeTasksItemUi.HomeTomorrowItemUi.TomorrowType.calendar,
                    listId = ++lastFakeTaskId,
                )
            )
        }

    return itemsUi.sortedWith { item1, item2 ->
        val timeData1: TextFeatures.TimeData? =
            item1.timeUi?.timeData
        val timeData2: TextFeatures.TimeData? =
            item2.timeUi?.timeData
        when {
            timeData1 != null && timeData2 != null ->
                if (timeData1.unixTime.time < timeData2.unixTime.time) -1 else 1
            timeData1 != null ->
                -1
            timeData2 != null ->
                1
            else ->
                if (item1.listId < item2.listId) -1 else 1
        }
    }
}
