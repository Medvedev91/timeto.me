package me.timeto.shared.ui.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import me.timeto.shared.AutoBackup
import me.timeto.shared.Backup
import me.timeto.shared.Cache
import me.timeto.shared.DeviceData
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
import me.timeto.shared.deviceData
import me.timeto.shared.launchExIo
import me.timeto.shared.prayEmoji
import me.timeto.shared.reportApi
import me.timeto.shared.utils.combine
import me.timeto.shared.vm.WhatsNewVm
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
        val privacyNote: String?,
        val todayOnHomeScreen: Boolean,
    ) {

        val headerTitle = "Settings"
        val readmeTitle = "How to Use the App"
        val whatsNewTitle = "What's New"
        val whatsNewNote: String =
            WhatsNewVm.prepHistoryItemsUi().first().timeAgoText

        val todayOnHomeScreenText = "Today on Home Screen"

        val dayStartNote: String = dayStartSecondsToString(dayStartSeconds)
        val dayStartListItems = (-6..6).map { hour ->
            DayStartOffsetListItem(
                seconds = hour * 3_600,
                note = dayStartSecondsToString(hour * 3_600)
            )
        }

        // Not used in iOS, but useful for report at -1
        val dayStartSelectedIdx = run {
            val index: Int =
                dayStartListItems.indexOfFirst { it.seconds == dayStartSeconds }
            if (index != -1)
                return@run index
            reportApi("SettingsSheetVM.dayStartSelectedIdx != -1")
            return@run dayStartListItems.indexOfFirst { it.seconds == 0 }
        }

        val infoText: String = run {
            val osName: String = when (deviceData.os) {
                is DeviceData.Os.Android -> "Android"
                is DeviceData.Os.Ios -> "iOS"
                is DeviceData.Os.Watchos -> "watchOS"
            }
            val flavor: String = deviceData.flavor?.let { "-$it" } ?: ""
            "timeto.me for $osName\nv${deviceData.version}.${deviceData.build}$flavor"
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
            privacyNote = KvDb.KEY.IS_SENDING_REPORTS.selectStringOrNullCached().privacyNote(),
            todayOnHomeScreen = KvDb.KEY.TODAY_ON_HOME_SCREEN.selectBooleanOrNullCached().todayOnHomeScreen(),
        )
    )

    init {
        val scopeVm = scopeVm()
        combine(
            ChecklistDb.selectAscFlow(),
            ShortcutDb.selectAscFlow(),
            NoteDb.selectAscFlow(),
            KvDb.KEY.DAY_START_OFFSET_SECONDS.selectStringOrNullFlow(),
            KvDb.KEY.IS_SENDING_REPORTS.selectStringOrNullFlow(),
            KvDb.KEY.TODAY_ON_HOME_SCREEN.selectBooleanOrNullFlow(),
            AutoBackup.lastTimeCache,
            KvDb.KEY.FEEDBACK_SUBJECT.selectStringOrNullFlow(),
        ) { checklistsDb: List<ChecklistDb>,
            shortcutsDb: List<ShortcutDb>,
            notesDb: List<NoteDb>,
            dayStartOffsetSeconds: String?,
            isSendingReports: String?,
            todayOnHomeScreen: Boolean?,
            autoBackupLastTime: UnixTime?,
            feedbackSubject: String?
            ->
            state.update {
                it.copy(
                    checklistsDb = checklistsDb,
                    shortcutsDb = shortcutsDb,
                    notesDb = notesDb,
                    dayStartSeconds = dayStartOffsetSeconds.asDayStartOffsetSeconds(),
                    privacyNote = isSendingReports.privacyNote(),
                    todayOnHomeScreen = todayOnHomeScreen.todayOnHomeScreen(),
                    autoBackupTimeString = prepAutoBackupTimeString(autoBackupLastTime),
                    feedbackSubject = feedbackSubject ?: DEFAULT_FEEDBACK_SUBJECT,
                )
            }
        }.launchIn(scopeVm)
    }

    fun setTodayOnHomeScreen(isOn: Boolean) {
        launchExIo {
            KvDb.KEY.TODAY_ON_HOME_SCREEN.upsertBool(isOn)
        }
    }

    fun setDayStartOffsetSeconds(seconds: Int) {
        launchExIo {
            KvDb.KEY.DAY_START_OFFSET_SECONDS.upsert(seconds.toString())
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

private fun String?.privacyNote(): String? =
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
