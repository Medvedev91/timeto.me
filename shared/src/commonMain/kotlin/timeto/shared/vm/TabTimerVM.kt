package timeto.shared.vm

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import timeto.shared.*
import timeto.shared.db.ActivityModel
import timeto.shared.db.IntervalModel
import timeto.shared.ui.IntervalNoteUI
import timeto.shared.ui.TimerHintUI

class TabTimerVM : __VM<TabTimerVM.State>() {

    class ActivityUI(
        val activity: ActivityModel,
        val noteUI: IntervalNoteUI?,
        val isActive: Boolean,
        val withTopDivider: Boolean,
    ) {

        val timerHints = TimerHintUI.buildList(
            activity,
            isShort = true,
            historyLimit = 2,
            customLimit = 5
        ) { seconds ->
            activity.startInterval(seconds)
        }

        val deletionHint: String
        val deletionConfirmation: String

        val textFeatures = activity.name.textFeatures()
        val listText = textFeatures.textUi()

        init {
            val nameWithEmojiNoTriggers = activity.nameWithEmoji().textFeatures().textUi()
            deletionHint = nameWithEmojiNoTriggers
            deletionConfirmation = "Are you sure you want to delete \"$nameWithEmojiNoTriggers\" activity?"
        }

        fun delete() {
            launchExDefault {
                try {
                    activity.delete()
                } catch (e: UIException) {
                    showUiAlert(e.uiMessage)
                }
            }
        }
    }

    data class State(
        val activities: List<ActivityModel>,
        val lastInterval: IntervalModel,
        val newAppData: NewAppData?,
    ) {
        val newActivityText = "New Activity"
        val sortActivitiesText = "Sort"
        val settingsText = "Settings"

        val activitiesUI = activities.toUiList(lastInterval)
    }

    override val state = MutableStateFlow(
        State(
            activities = DI.activitiesSorted,
            lastInterval = DI.lastInterval,
            newAppData = null,
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        ActivityModel.getAscSortedFlow()
            .onEachExIn(scope) { activities ->
                state.update { it.copy(activities = activities) }
            }
        IntervalModel.getLastOneOrNullFlow()
            .filterNotNull()
            .onEachExIn(scope) { interval ->
                state.update { it.copy(lastInterval = interval) }
            }

        launchExDefault {
            HttpClient().use { client ->
                val httpResponse = client.get("https://api.timeto.me/new_app_message") {
                    url {
                        appendDeviceData()
                    }
                }
                val plainJson = httpResponse.bodyAsText()
                val j = Json.parseToJsonElement(plainJson).jsonObject
                if (!j.getBoolean("is_active"))
                    return@use
                val newAppData = NewAppData(
                    message = j.getString("message"),
                    btn_text = j.getString("btn_text"),
                    btn_url = j.getString("btn_url"),
                )
                state.update { it.copy(newAppData = newAppData) }
            }
        }
    }

    // todo remove
    data class NewAppData(
        val message: String,
        val btn_text: String,
        val btn_url: String,
    )
}

private fun List<ActivityModel>.toUiList(
    lastInterval: IntervalModel
): List<TabTimerVM.ActivityUI> {
    val sorted = this.sortedWith(compareBy({ it.sort }, { it.id }))
    val activeIdx = sorted.indexOfFirst { it.id == lastInterval.activity_id }
    return sorted.mapIndexed { idx, activity ->
        val isActive = (idx == activeIdx)
        val noteUI = if (isActive && lastInterval.note != null)
            IntervalNoteUI(lastInterval.note, checkLeadingEmoji = true)
        else null
        TabTimerVM.ActivityUI(
            activity = activity,
            noteUI = noteUI,
            isActive = isActive,
            withTopDivider = (idx != 0) && (activeIdx != idx - 1),
        )
    }
}
