package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.EventTemplateDb
import me.timeto.shared.libs.DaytimeModel

class EventTemplateFormSheetVM(
    val eventTemplateDB: EventTemplateDb?,
) : __VM<EventTemplateFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val doneText: String,
        val textFeatures: TextFeatures,
        val daytimeModel: DaytimeModel?,
    ) {

        val daytimeTitle = "Time"

        val daytimeNote: String = daytimeModel?.text ?: "None"
        val daytimeNoteColor: ColorRgba? = if (daytimeModel != null) null else ColorRgba.red

        val defDaytimeModel: DaytimeModel = daytimeModel ?: DaytimeModel(hour = 12, minute = 0)

        val inputTextValue = textFeatures.textNoFeatures
        val deleteText = "Delete Template"
    }

    override val state = MutableStateFlow(
        State(
            headerTitle = if (eventTemplateDB != null) "Edit Template" else "New Template",
            doneText = "Save",
            textFeatures = (eventTemplateDB?.text ?: "").textFeatures(),
            daytimeModel = eventTemplateDB?.daytime?.let { DaytimeModel.byHms(it) },
        )
    )

    fun setInputTextValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun setTextFeatures(newTextFeatures: TextFeatures) = state.update {
        it.copy(textFeatures = newTextFeatures)
    }

    fun setDaytime(daytimeModel: DaytimeModel?) {
        state.update { it.copy(daytimeModel = daytimeModel) }
    }

    fun save(
        onSuccess: () -> Unit,
    ) {
        scopeVM().launchEx {
            try {
                val daytime = state.value.daytimeModel?.seconds ?: throw UIException("The time is not set")
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
