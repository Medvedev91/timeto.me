package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.EventDb
import me.timeto.shared.libs.DaytimeModel

/**
 * Different logic for platform. For iOS, we use a Date() object
 * for date and time, for Android there are custom pickers.
 */
class EventFormSheetVm(
    val event: EventDb?,
    val defText: String?,
    val defTime: Int?,
) : __Vm<EventFormSheetVm.State>() {

    data class State(
        val saveText: String,
        val textFeatures: TextFeatures,
        val unixDay: Int,
        val daytimeModel: DaytimeModel,
    ) {

        val selectedUnixTime: UnixTime = UnixTime.byLocalDay(unixDay).inSeconds(
            daytimeModel.hour * 3_600 + daytimeModel.minute * 60
        )

        val inputTextValue = textFeatures.textNoFeatures
        val inputPlaceholder = "Event"
        val minTime = UnixTime().localDayStartTime()

        // Only for Android
        val selectedDateText: String = selectedUnixTime.getStringByComponents(
            UnixTime.StringComponent.dayOfMonth,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.month3,
            UnixTime.StringComponent.comma,
            UnixTime.StringComponent.space,
            UnixTime.StringComponent.dayOfWeek3,
        )
    }

    override val state: MutableStateFlow<State>

    init {
        val textFeatures = (event?.text ?: defText ?: "").textFeatures()
        val initTime: Int = event?.getLocalTime()?.time ?: defTime ?: UnixTime().localDayStartTime()
        val initUnixTime = UnixTime(initTime)
        val hms = (initUnixTime.time - initUnixTime.localDayStartTime()).toHms()
        state = MutableStateFlow(
            State(
                saveText = if (event != null) "Save" else "Create",
                textFeatures = textFeatures,
                unixDay = initUnixTime.localDay,
                daytimeModel = DaytimeModel(hour = hms[0], minute = hms[1]),
            )
        )
    }

    fun setDaytime(daytimeModel: DaytimeModel) {
        state.update { it.copy(daytimeModel = daytimeModel) }
    }

    fun setUnixDay(unixDay: Int) {
        state.update { it.copy(unixDay = unixDay) }
    }

    fun setInputTextValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun setTemplate(templateUi: EventTemplatesVm.TemplateUI) {
        state.update {
            it.copy(
                textFeatures = templateUi.templateDB.text.textFeatures(),
                daytimeModel = DaytimeModel.byDaytime(templateUi.templateDB.daytime),
            )
        }
    }

    fun save(
        onSuccess: () -> Unit
    ) = scopeVm().launchEx {
        try {
            // todo check if a text without features
            val nameWithFeatures = state.value.textFeatures.textWithFeatures()
            val time = state.value.selectedUnixTime.time

            if (event != null) {
                event.upWithValidation(
                    text = nameWithFeatures,
                    localTime = time,
                )
            } else {
                EventDb.addWithValidation(
                    text = nameWithFeatures,
                    localTime = time,
                )
            }
            onSuccess()
            EventDb.syncTodaySafe(UnixTime().localDay)
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
