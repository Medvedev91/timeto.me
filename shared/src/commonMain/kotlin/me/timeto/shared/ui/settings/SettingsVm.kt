package me.timeto.shared.ui.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.AutoBackup
import me.timeto.shared.Backup
import me.timeto.shared.Cache
import me.timeto.shared.UnixTime
import me.timeto.shared.backupStateFlow
import me.timeto.shared.dayStartOffsetSeconds
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.asDayStartOffsetSeconds
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.db.NoteDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.deviceData
import me.timeto.shared.launchEx
import me.timeto.shared.launchExDefault
import me.timeto.shared.launchExIo
import me.timeto.shared.onEachExIn
import me.timeto.shared.reportApi
import me.timeto.shared.vm.PrivacySheetVm
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
        val whatsNewNote: String = WhatsNewVm.prepHistoryItemsUi().first().timeAgoText

        val todayOnHomeScreenText = "Today on Home Screen"

        val dayStartNote = dayStartSecondsToString(dayStartSeconds)
        val dayStartListItems = (-6..6).map { hour ->
            DayStartOffsetListItem(
                seconds = hour * 3_600,
                note = dayStartSecondsToString(hour * 3_600)
            )
        }

        // Not used in iOS, but useful for report at -1
        val dayStartSelectedIdx = run {
            val index = dayStartListItems.indexOfFirst { it.seconds == dayStartSeconds }
            if (index != -1)
                return@run index
            reportApi("SettingsSheetVM.dayStartSelectedIdx != -1")
            return@run dayStartListItems.indexOfFirst { it.seconds == 0 }
        }

        val appVersion = deviceData.build.toString()
    }

    override val state = MutableStateFlow(
        State(
            checklistsDb = Cache.checklistsDb,
            shortcutsDb = Cache.shortcutsDb,
            notesDb = Cache.notesDb,
            dayStartSeconds = dayStartOffsetSeconds(),
            feedbackSubject = "Feedback",
            autoBackupTimeString = prepAutoBackupTimeString(AutoBackup.lastTimeCache.value),
            privacyNote = null, // todo init value
            todayOnHomeScreen = KvDb.todayOnHomeScreenCached(),
        )
    )

    init {
        val scope = scopeVm()
        ChecklistDb.selectAscFlow()
            .onEachExIn(scope) { checklists -> state.update { it.copy(checklistsDb = checklists) } }
        ShortcutDb.selectAscFlow()
            .onEachExIn(scope) { shortcuts -> state.update { it.copy(shortcutsDb = shortcuts) } }
        NoteDb.selectAscFlow()
            .onEachExIn(scope) { notes -> state.update { it.copy(notesDb = notes) } }
        KvDb.KEY.DAY_START_OFFSET_SECONDS
            .getOrNullFlow()
            .onEachExIn(scope) { kv ->
                val seconds = kv?.value.asDayStartOffsetSeconds()
                state.update { it.copy(dayStartSeconds = seconds) }
            }
        KvDb.KEY.IS_SENDING_REPORTS
            .getOrNullFlow()
            .onEachExIn(scope) { kv ->
                val isEnabled = kv?.value.isSendingReports()
                state.update { it.copy(privacyNote = if (isEnabled) null else PrivacySheetVm.prayEmoji) }
            }
        KvDb.KEY.TODAY_ON_HOME_SCREEN
            .selectBooleanOrNullFlow()
            .onEachExIn(scope) { kvValue ->
                state.update {
                    it.copy(todayOnHomeScreen = kvValue ?: KvDb.TODAY_ON_HOME_SCREEN_DEFAULT)
                }
            }
        AutoBackup.lastTimeCache.onEachExIn(scope) { unixTime ->
            state.update { it.copy(autoBackupTimeString = prepAutoBackupTimeString(unixTime)) }
        }
        scope.launchEx {
            val subject = KvDb.KEY.FEEDBACK_SUBJECT.selectOrNull()
            if (subject != null)
                state.update { it.copy(feedbackSubject = subject) }
        }
    }

    fun setTodayOnHomeScreen(isOn: Boolean) {
        launchExIo {
            KvDb.KEY.TODAY_ON_HOME_SCREEN.upsertBool(isOn)
        }
    }

    fun upDayStartOffsetSeconds(
        seconds: Int,
        onSuccess: () -> Unit,
    ) {
        scopeVm().launchEx {
            KvDb.KEY.DAY_START_OFFSET_SECONDS.upsert(seconds.toString())
            onUi { onSuccess() }
        }
    }

    fun procRestore(
        jString: String,
    ) {
        launchExDefault {
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

    fun prepBackupFileName() = Backup.prepFileName(UnixTime(), prefix = "timetome_")

    companion object {

        private fun dayStartSecondsToString(seconds: Int): String {
            if ((seconds % 3_600) != 0) {
                reportApi("Invalid seconds in TabToolsVM.dayStartSecondsToString($seconds)")
                return "error"
            }

            val h = seconds / 3_600

            if (h >= 0)
                return "$h:00".padStart(5, '0')

            return "${24 + h}:00".padStart(5, '0')
        }

        private fun prepAutoBackupTimeString(
            unixTime: UnixTime?
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
                UnixTime.StringComponent.hhmm24
            )
        }
    }
}
