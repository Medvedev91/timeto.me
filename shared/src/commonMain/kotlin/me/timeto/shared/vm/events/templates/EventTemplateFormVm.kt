package me.timeto.shared.vm.events.templates

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.Cache
import me.timeto.shared.TextFeatures
import me.timeto.shared.db.ChecklistDb
import me.timeto.shared.db.EventTemplateDb
import me.timeto.shared.db.ShortcutDb
import me.timeto.shared.launchExIo
import me.timeto.shared.DaytimeUi
import me.timeto.shared.textFeatures
import me.timeto.shared.toTimerHintNote
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.vm.Vm

class EventTemplateFormVm(
    val initEventTemplateDb: EventTemplateDb?,
) : Vm<EventTemplateFormVm.State>() {

    data class State(
        val title: String,
        val doneText: String,
        val textFeatures: TextFeatures,
        val daytimeUi: DaytimeUi?,
    ) {

        val daytimeTitle = "Time"
        val daytimeNote: String =
            daytimeUi?.text ?: "Not Selected"
        val daytimeUiPicker: DaytimeUi =
            daytimeUi ?: DaytimeUi(hour = 12, minute = 0)

        val text: String = textFeatures.textNoFeatures
        val textPlaceholder = "Text"

        val deleteText = "Delete Template"

        val goalDb: Goal2Db? = textFeatures.goalDb
        val goalTitle = "Goal"
        val goalNote: String =
            goalDb?.name?.textFeatures()?.textNoFeatures ?: "Not Selected"
        val goalsUi: List<GoalUi> =
            Cache.goals2Db.map { GoalUi(it) }

        val timerSeconds: Int? = textFeatures.timer
        val timerSecondsPicker: Int = timerSeconds ?: (45 * 60)
        val timerTitle = "Timer"
        val timerNote: String =
            timerSeconds?.toTimerHintNote(isShort = false) ?: "Not Selected"

        val checklistsDb: List<ChecklistDb> = textFeatures.checklistsDb
        val checklistsTitle = "Checklists"
        val checklistsNote: String =
            if (checklistsDb.isEmpty()) "None"
            else checklistsDb.joinToString(", ") { it.name }

        val shortcutsDb: List<ShortcutDb> = textFeatures.shortcutsDb
        val shortcutsTitle = "Shortcuts"
        val shortcutsNote: String =
            if (shortcutsDb.isEmpty()) "None"
            else shortcutsDb.joinToString(", ") { it.name }
    }

    override val state = MutableStateFlow(
        State(
            title = if (initEventTemplateDb != null) "Edit Template" else "New Template",
            doneText = "Save",
            textFeatures = (initEventTemplateDb?.text ?: "").textFeatures(),
            daytimeUi = initEventTemplateDb?.daytime?.let { DaytimeUi.byDaytime(it) },
        )
    )

    fun setText(text: String) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
        }
    }

    fun setDaytime(daytimeUi: DaytimeUi) {
        state.update { it.copy(daytimeUi = daytimeUi) }
    }

    fun setGoal(goalDb: Goal2Db?) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(goalDb = goalDb))
        }
    }

    fun setTimer(seconds: Int) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(timer = seconds))
        }
    }

    fun setChecklists(checklistsDb: List<ChecklistDb>) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(checklistsDb = checklistsDb))
        }
    }

    fun setShortcuts(shortcutsDb: List<ShortcutDb>) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(shortcutsDb = shortcutsDb))
        }
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ): Unit = launchExIo {
        try {
            val eventTemplateDb: EventTemplateDb? = initEventTemplateDb
            val daytime: Int =
                state.value.daytimeUi?.seconds ?: throw UiException("Time is not set")
            val textFeatures: TextFeatures =
                state.value.textFeatures
            val textWithFeatures: String =
                textFeatures.textWithFeatures()
            if (textFeatures.textNoFeatures.isBlank())
                throw UiException("Text is empty")
            if (textFeatures.goalDb == null)
                throw UiException("Goal not selected")
            if (textFeatures.timer == null)
                throw UiException("Timer not selected")
            if (eventTemplateDb != null)
                eventTemplateDb.updateWithValidation(daytime, textWithFeatures)
            else
                EventTemplateDb.insertWithValidation(daytime, textWithFeatures)
            onUi {
                onSuccess()
            }
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        eventTemplateDb: EventTemplateDb,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        val text: String =
            eventTemplateDb.text.textFeatures().textNoFeatures
        dialogsManager.confirmation(
            message = "Remove \"$text\" from templates?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    eventTemplateDb.backupable__delete()
                    onUi {
                        onSuccess()
                    }
                }
            },
        )
    }

    ///

    data class GoalUi(
        val goalDb: Goal2Db,
    ) {
        val title: String =
            goalDb.name.textFeatures().textNoFeatures
    }
}
