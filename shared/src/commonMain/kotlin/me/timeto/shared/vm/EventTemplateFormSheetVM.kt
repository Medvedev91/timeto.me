package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.EventTemplateDB

class EventTemplateFormSheetVM(
    private val eventTemplateDB: EventTemplateDB?,
) : __VM<EventTemplateFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val doneText: String,
        val textFeatures: TextFeatures,
        val daytime: Int?,
    ) {

        val daytimeTitle = "Time"
        val daytimeDefHour: Int
        val daytimeDefMinute: Int
        val daytimeNote = daytime?.let { daytimeToString(it) } ?: "None"
        val daytimeNoteColor: ColorRgba? = if (daytime != null) null else ColorRgba.red

        val inputTextValue = textFeatures.textNoFeatures
        val deleteText = "Delete Template"

        init {
            if (daytime != null) {
                val (h, m) = daytime.toHms()
                daytimeDefHour = h
                daytimeDefMinute = m
            } else {
                daytimeDefHour = 12
                daytimeDefMinute = 0
            }
        }
    }

    override val state = MutableStateFlow(
        State(
            headerTitle = if (eventTemplateDB != null) "Edit Template" else "New Template",
            doneText = "Save",
            textFeatures = (eventTemplateDB?.text ?: "").textFeatures(),
            daytime = eventTemplateDB?.daytime,
        )
    )

    fun setInputTextValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun setTextFeatures(newTextFeatures: TextFeatures) = state.update {
        it.copy(textFeatures = newTextFeatures)
    }

    fun setDaytime(newDaytimeOrNull: Int?) {
        state.update { it.copy(daytime = newDaytimeOrNull) }
    }

    fun save(
        onSuccess: () -> Unit,
    ) {
        scopeVM().launchEx {
            try {
                val daytime = state.value.daytime ?: throw UIException("The time is not set")
                val textFeatures = state.value.textFeatures
                val textWithFeatures = textFeatures.textWithFeatures()
                if (textFeatures.textNoFeatures.isBlank())
                    throw UIException("Text is empty")
                if (textFeatures.activity == null)
                    throw UIException("Activity not selected")
                if (textFeatures.timer == null)
                    throw UIException("Timer not selected")
                if (eventTemplateDB != null)
                    eventTemplateDB.updateWithValidation(daytime, textWithFeatures)
                else
                    EventTemplateDB.insertWithValidation(daytime, textWithFeatures)
                onSuccess()
            } catch (e: UIException) {
                showUiAlert(e.uiMessage)
            }
        }
    }

    fun delete(
        templateDB: EventTemplateDB,
        onSuccess: () -> Unit,
    ) {
        val text = templateDB.text.textFeatures().textNoFeatures
        showUiConfirmation(
            UIConfirmationData(
                text = "Remove \"$text\" from templates?",
                buttonText = "Remove",
                isRed = true,
            ) {
                scopeVM().launchEx {
                    templateDB.backupable__delete()
                    onSuccess()
                }
            }
        )
    }
}
