package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.EventTemplateDb
import me.timeto.shared.vm.ui.DaytimePickerUi

class EventTemplateFormSheetVM(
    val eventTemplateDB: EventTemplateDb?,
) : __VM<EventTemplateFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val doneText: String,
        val textFeatures: TextFeatures,
        val daytimePickerUi: DaytimePickerUi?,
    ) {

        val daytimeTitle = "Time"

        val daytimeNote: String = daytimePickerUi?.text ?: "None"
        val daytimeNoteColor: ColorRgba? = if (daytimePickerUi != null) null else ColorRgba.red

        val defDaytimePickerUi = daytimePickerUi ?: DaytimePickerUi(hour = 12, minute = 0)

        val inputTextValue = textFeatures.textNoFeatures
        val deleteText = "Delete Template"
    }

    override val state = MutableStateFlow(
        State(
            headerTitle = if (eventTemplateDB != null) "Edit Template" else "New Template",
            doneText = "Save",
            textFeatures = (eventTemplateDB?.text ?: "").textFeatures(),
            daytimePickerUi = eventTemplateDB?.daytime?.let { DaytimePickerUi.byHms(it) },
        )
    )

    fun setInputTextValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun setTextFeatures(newTextFeatures: TextFeatures) = state.update {
        it.copy(textFeatures = newTextFeatures)
    }

    fun setDaytime(daytimePickerUi: DaytimePickerUi?) {
        state.update { it.copy(daytimePickerUi = daytimePickerUi) }
    }

    fun save(
        onSuccess: () -> Unit,
    ) {
        scopeVM().launchEx {
            try {
                val daytime = state.value.daytimePickerUi?.seconds ?: throw UIException("The time is not set")
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
                    EventTemplateDb.insertWithValidation(daytime, textWithFeatures)
                onSuccess()
            } catch (e: UIException) {
                showUiAlert(e.uiMessage)
            }
        }
    }

    fun delete(
        templateDB: EventTemplateDb,
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
