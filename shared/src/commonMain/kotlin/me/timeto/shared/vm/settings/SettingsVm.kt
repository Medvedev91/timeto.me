package me.timeto.shared.vm.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import me.timeto.shared.backups.AutoBackup
import me.timeto.shared.backups.Backup
import me.timeto.shared.Cache
import me.timeto.shared.HomeButtonSort
import me.timeto.shared.SystemInfo
import me.timeto.shared.UnixTime
import me.timeto.shared.dayStartOffsetSeconds
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.db.KvDb.Companion.todayOnHomeScreen
import me.timeto.shared.db.NoteDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchExIo
import me.timeto.shared.prayEmoji
import me.timeto.shared.reportApi
import me.timeto.shared.combine
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.vm.app.AppVm.Companion.backupStateFlow
import me.timeto.shared.vm.whats_new.WhatsNewVm
import me.timeto.shared.vm.Vm

class SettingsVm : Vm<SettingsVm.State>() {

    data class DayStartOffsetListItem(
        val seconds: Int,
        val note: String,
    )

    data class State(
        val goalsUi: List<GoalUi>,
        val checklistsDb: List<ChecklistDb>,
        val shortcutsDb: List<ShortcutDb>,
        val notesDb: List<NoteDb>,
        val dayStartSeconds: Int,
        val feedbackSubject: String,
        val autoBackupTimeString: String,
        val privacyEmoji: String?,
        val todayOnHomeScreen: Boolean,
    ) {

        val headerTitle = "Settings"
        val readmeTitle = "How to Use the App"
        val whatsNewTitle = "What's New"
        val whatsNewNote: String =
            WhatsNewVm.historyItemsUi.first().timeAgoText

        val goalsTitle = "Edit Goals"
        val todayOnHomeScreenText = "Today on Home Screen"

        val dayStartNote: String = dayStartSecondsToString(dayStartSeconds)
        val dayStartListItems = (-8..8).map { hour ->
            DayStartOffsetListItem(
                seconds = hour * 3_600,
                note = dayStartSecondsToString(hour * 3_600)
            )
        }

        val infoText: String = run {
            val systemInfo = SystemInfo.instance
            val osName: String = when (systemInfo.os) {
                is SystemInfo.Os.Android -> "Android"
                is SystemInfo.Os.Ios -> "iOS"
                is SystemInfo.Os.Watchos -> "watchOS"
            }
            val flavor: String = systemInfo.flavor?.let { "-$it" } ?: ""
            "timeto.me for $osName\nv${systemInfo.version}.${systemInfo.build}$flavor"
        }
    }

    override val state = MutableStateFlow(
        State(
            goalsUi = GoalUi.buildList(Cache.goals2Db),
            checklistsDb = Cache.checklistsDb,
            shortcutsDb = Cache.shortcutsDb,
            notesDb = Cache.notesDb,
            dayStartSeconds = dayStartOffsetSeconds(),
            feedbackSubject = DEFAULT_FEEDBACK_SUBJECT,
            autoBackupTimeString = prepAutoBackupTimeString(AutoBackup.lastTimeCache.value),
            privacyEmoji = KvDb.KEY.IS_SENDING_REPORTS.selectOrNullCached().privacyEmojiOrNull(),
            todayOnHomeScreen = KvDb.KEY.TODAY_ON_HOME_SCREEN.selectOrNullCached().todayOnHomeScreen(),
        )
    )

    init {
        val scopeVm = scopeVm()
        combine(
            Goal2Db.selectAllFlow(),
            ChecklistDb.selectAscFlow(),
            ShortcutDb.selectAscFlow(),
            NoteDb.selectAscFlow(),
            KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNullFlow(),
            KvDb.KEY.IS_SENDING_REPORTS.selectOrNullFlow(),
            KvDb.KEY.TODAY_ON_HOME_SCREEN.selectOrNullFlow(),
            AutoBackup.lastTimeCache,
            KvDb.KEY.FEEDBACK_SUBJECT.selectStringOrNullFlow(),
        ) {
                goalsDb: List<Goal2Db>,
                checklistsDb: List<ChecklistDb>,
                shortcutsDb: List<ShortcutDb>,
                notesDb: List<NoteDb>,
                dayStartOffsetSeconds: KvDb?,
                isSendingReports: KvDb?,
                todayOnHomeScreen: KvDb?,
                autoBackupLastTime: UnixTime?,
                feedbackSubject: String?,
            ->
            state.update {
                it.copy(
                    goalsUi = GoalUi.buildList(goalsDb),
                    checklistsDb = checklistsDb,
                    shortcutsDb = shortcutsDb,
                    notesDb = notesDb,
                    dayStartSeconds = dayStartOffsetSeconds.asDayStartOffsetSeconds(),
                    privacyEmoji = isSendingReports.privacyEmojiOrNull(),
                    todayOnHomeScreen = todayOnHomeScreen.todayOnHomeScreen(),
                    autoBackupTimeString = prepAutoBackupTimeString(autoBackupLastTime),
                    feedbackSubject = feedbackSubject ?: DEFAULT_FEEDBACK_SUBJECT,
                )
            }
        }.launchIn(scopeVm)
    }

    fun startInterval(goalDb: Goal2Db, seconds: Int) {
        launchExIo {
            goalDb.startInterval(timer = seconds)
        }
    }

    fun setTodayOnHomeScreen(isOn: Boolean) {
        launchExIo {
            KvDb.KEY.TODAY_ON_HOME_SCREEN.upsertBoolean(isOn)
        }
    }

    fun setDayStartOffsetSeconds(seconds: Int) {
        launchExIo {
            KvDb.KEY.DAY_START_OFFSET_SECONDS.upsertInt(seconds)
        }
    }

    fun procRestore(jString: String) {
        launchExIo {
            try {
                backupStateFlow.emit("Loading..")
                Backup.restore(jString)
                backupStateFlow.emit("Please restart the app.")
            } catch (e: Throwable) {
                backupStateFlow.emit("Error")
                reportApi("SettingsSheetVM.procRestore() error:\n$e")
            }
        }
    }

    fun prepBackupFileName(): String =
        Backup.prepFileName(UnixTime(), prefix = "timetome_")

    ///

    data class GoalUi(
        val goalDb: Goal2Db,
        val nestedLevel: Int,
    ) {

        companion object {

            fun buildList(goalsDb: List<Goal2Db>): List<GoalUi> {
                val resList: MutableList<GoalUi> = mutableListOf()
                val sortedGoalsDb: List<Goal2Db> = goalsDb.sortedWith { goalDb1, goalDb2 ->
                    val sort1: HomeButtonSort =
                        HomeButtonSort.parseOrNull(goalDb1.home_button_sort) ?: HomeButtonSort(0, 0, 0)
                    val sort2: HomeButtonSort =
                        HomeButtonSort.parseOrNull(goalDb2.home_button_sort) ?: HomeButtonSort(0, 0, 0)
                    if (sort1.rowIdx != sort2.rowIdx)
                        return@sortedWith if (sort1.rowIdx > sort2.rowIdx) 1 else -1
                    if (sort1.cellIdx > sort2.cellIdx) 1 else -1
                }

                fun addRecursive(goalDb: Goal2Db, nestedLevel: Int) {
                    resList.add(GoalUi(goalDb = goalDb, nestedLevel = nestedLevel))
                    sortedGoalsDb.filter { it.parent_id == goalDb.id }.forEach { childrenGoalDb ->
                        addRecursive(childrenGoalDb, nestedLevel + 1)
                    }
                }
                sortedGoalsDb.filter { it.parent_id == null }.forEach { goalDb ->
                    addRecursive(goalDb, nestedLevel = 0)
                }
                return resList
            }
        }

        ///

        val title: String =
            goalDb.name.textFeatures().textNoFeatures

        val timerHintsUi: List<TimerHintUi> =
            listOf(5 * 60, 15 * 60, 45 * 60).map { seconds ->
                TimerHintUi(
                    seconds = seconds,
                    onTap = {
                        launchExIo {
                            goalDb.startInterval(
                                timer = seconds,
                            )
                        }
                    },
                )
            }

        ///

        class TimerHintUi(
            val seconds: Int,
            val onTap: () -> Unit,
        ) {
            val title: String =
                seconds.toTimerHintNote(isShort = true)
        }
    }
}

///

private const val DEFAULT_FEEDBACK_SUBJECT = "Feedback"

private fun KvDb?.privacyEmojiOrNull(): String? =
    if (this.isSendingReports()) null else prayEmoji

private fun dayStartSecondsToString(seconds: Int): String {
    if ((seconds % 3_600) != 0) {
        reportApi("Invalid seconds in SettingsVm.dayStartSecondsToString($seconds)")
        return "error"
    }
    val h = seconds / 3_600
    if (h >= 0)
        return "$h:00".padStart(5, '0')
    return "${24 + h}:00".padStart(5, '0')
}

private fun prepAutoBackupTimeString(
    unixTime: UnixTime?,
): String {
    if (unixTime == null)
        return ""
    return unixTime.getStringByComponents(
        UnixTime.StringComponent.dayOfMonth,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.month3,
        UnixTime.StringComponent.comma,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.dayOfWeek3,
        UnixTime.StringComponent.space,
        UnixTime.StringComponent.hhmm24,
    )
}
