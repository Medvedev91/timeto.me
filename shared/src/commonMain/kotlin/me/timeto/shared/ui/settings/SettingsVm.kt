package me.timeto.shared.ui.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import me.timeto.shared.misc.backups.AutoBackup
import me.timeto.shared.Backup
import me.timeto.shared.Cache
import me.timeto.shared.misc.SystemInfo
import me.timeto.shared.UnixTime
import me.timeto.shared.backupStateFlow
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
import me.timeto.shared.misc.combine
import me.timeto.shared.ui.whats_new.WhatsNewVm
import me.timeto.shared.vm.__Vm

class SettingsVm : __Vm<SettingsVm.State>() {

    data class DayStartOffsetListItem(
        val seconds: Int,
        val note: String,
    )

    data class State(
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

        val todayOnHomeScreenText = "Today on Home Screen"

        val dayStartNote: String = dayStartSecondsToString(dayStartSeconds)
        val dayStartListItems = (-6..6).map { hour ->
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
            ChecklistDb.selectAscFlow(),
            ShortcutDb.selectAscFlow(),
            NoteDb.selectAscFlow(),
            KvDb.KEY.DAY_START_OFFSET_SECONDS.selectOrNullFlow(),
            KvDb.KEY.IS_SENDING_REPORTS.selectOrNullFlow(),
            KvDb.KEY.TODAY_ON_HOME_SCREEN.selectOrNullFlow(),
            AutoBackup.lastTimeCache,
            KvDb.KEY.FEEDBACK_SUBJECT.selectStringOrNullFlow(),
        ) { checklistsDb: List<ChecklistDb>,
            shortcutsDb: List<ShortcutDb>,
            notesDb: List<NoteDb>,
            dayStartOffsetSeconds: KvDb?,
            isSendingReports: KvDb?,
            todayOnHomeScreen: KvDb?,
            autoBackupLastTime: UnixTime?,
            feedbackSubject: String?
            ->
            state.update {
                it.copy(
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
