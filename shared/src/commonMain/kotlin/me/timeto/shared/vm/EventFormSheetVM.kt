package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.EventDb
import me.timeto.shared.vm.ui.DaytimePickerUi

/**
 * Different logic for platform. For iOS, we use a Date() object
 * for date and time, for Android there are custom pickers.
 */
class EventFormSheetVM(
    val event: EventDb?,
    val defText: String?,
    val defTime: Int?,
) : __VM<EventFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val textFeatures: TextFeatures,
        val unixDay: Int,
        val daytimePickerUi: DaytimePickerUi,
    ) {

        val selectedUnixTime = UnixTime.byLocalDay(unixDay).inSeconds(
            daytimePickerUi.hour * 3_600 + daytimePickerUi.minute * 60
        )

        val inputTextValue = textFeatures.textNoFeatures
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
                headerTitle = if (event != null) "Edit Event" else "New Event",
                headerDoneText = if (event != null) "Save" else "Create",
                textFeatures = textFeatures,
                unixDay = initUnixTime.localDay,
                daytimePickerUi = DaytimePickerUi(hour = hms[0], minute = hms[1]),
            )
        )
    }

    fun setDaytime(newDaytimePickerUi: DaytimePickerUi) {
        state.update { it.copy(daytimePickerUi = newDaytimePickerUi) }
    }

    fun setUnixDay(newUnixDay: Int) {
        state.update { it.copy(unixDay = newUnixDay) }
    }

    fun setInputTextValue(text: String) = state.update {
        it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
    }

    fun save(
        onSuccess: () -> Unit
    ) = scopeVM().launchEx {
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
