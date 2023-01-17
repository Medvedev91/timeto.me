package timeto.shared.vm

import kotlinx.coroutines.flow.*
import timeto.shared.*
import timeto.shared.db.ChecklistModel
import timeto.shared.db.KVModel
import timeto.shared.db.ShortcutModel

class TabToolsVM : __VM<TabToolsVM.State>() {

    data class DayStartOffsetListItem(
        val seconds: Int,
        val note: String,
    )

    data class State(
        val shortcuts: List<ShortcutModel>,
        val checklists: List<ChecklistModel>,
        val dayStartSeconds: Int,
        val feedbackSubject: String,
    ) {

        val openSourceUrl = "https://github.com/timeto-app/timeto_app"
        val feedbackEmail = "hi@timeto.me"

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
            reportApi("TabToolsVM.dayStartSelectedIdx != -1")
            return@run dayStartListItems.indexOfFirst { it.seconds == 0 }
        }

        val appVersion = deviceData.build.toString()
    }

    override val state = MutableStateFlow(
        State(
            shortcuts = DI.shortcuts,
            checklists = DI.checklists,
            dayStartSeconds = dayStartOffsetSeconds(),
            feedbackSubject = "Feedback"
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ShortcutModel.getAscFlow()
            .onEachExIn(scope) { shortcuts -> state.update { it.copy(shortcuts = shortcuts) } }
        ChecklistModel.getAscFlow()
            .onEachExIn(scope) { checklists -> state.update { it.copy(checklists = checklists) } }
        KVModel.getByKeyOrNullFlow(KVModel.KEY.DAY_START_OFFSET_SECONDS)
            .onEachExIn(scope) { kv ->
                val seconds = kv?.value?.toInt() ?: KVModel.DAY_START_OFFSET_SECONDS_DEFAULT
                state.update { it.copy(dayStartSeconds = seconds) }
            }
        scope.launchEx {
            val subject = SecureLocalStorage__Key.feedback_subject.getOrNull()
            if (subject != null)
                state.update { it.copy(feedbackSubject = subject) }
        }
    }

    fun upDayStartOffsetSeconds(
        seconds: Int,
        onSuccess: () -> Unit,
    ) {
        scopeVM().launchEx {
            KVModel.KEY.DAY_START_OFFSET_SECONDS.upsert(seconds.toString())
            onSuccess()
        }
    }

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
    }
}
