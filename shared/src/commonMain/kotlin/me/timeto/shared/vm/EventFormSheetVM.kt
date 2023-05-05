package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.EventModel

/**
 * Different logic for platform. For iOS, we use a Date() object
 * for date and time, for Android there are custom pickers.
 */
class EventFormSheetVM(
    val event: EventModel?,
    val defText: String?,
    val defTime: Int?,
) : __VM<EventFormSheetVM.State>() {

    data class State(
        val headerTitle: String,
        val headerDoneText: String,
        val selectedTime: Int,
        val isAutoFocus: Boolean,
        val textFeatures: TextFeatures,
    ) {
        val inputTextValue = textFeatures.textNoFeatures
        val minTime = UnixTime().localDayStartTime()
        val isHeaderDoneEnabled =
            inputTextValue.isNotBlank() && (UnixTime(selectedTime).localDay > UnixTime().localDay)

        // Only for Android
        val dayStartTime: Int = UnixTime(selectedTime).localDayStartTime()
        val hour: Int
        val minute: Int

        init {
            val hms = (selectedTime - dayStartTime).toHms()
            hour = hms[0]
            minute = hms[1]
        }
    }

    override val state: MutableStateFlow<State>

    init {
        val textFeatures = (event?.text ?: defText ?: "").textFeatures()
        state = MutableStateFlow(
            State(
                headerTitle = if (event != null) "Edit Event" else "New Event",
                headerDoneText = if (event != null) "Done" else "Create",
                selectedTime = event?.getLocalTime()?.time ?: defTime ?: UnixTime().localDayStartTime(),
                isAutoFocus = (event == null && textFeatures.textNoFeatures.isBlank()),
                textFeatures = textFeatures,
            )
        )
    }

    fun setTime(time: Int) {
        state.update { it.copy(selectedTime = time) }
    }

    fun setTimeByComponents(
        dayStartTime: Int = state.value.dayStartTime,
        hour: Int = state.value.hour,
        minute: Int = state.value.minute,
    ) {
        setTime(dayStartTime + (hour * 3600) + (minute * 60))
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

            if (event != null) {
                event.upWithValidation(
                    text = nameWithFeatures,
                    localTime = state.value.selectedTime,
                )
            } else {
                EventModel.addWithValidation(
                    text = nameWithFeatures,
                    localTime = state.value.selectedTime,
                    addToHistory = true,
                )
            }
            onSuccess()
        } catch (e: UIException) {
            showUiAlert(e.uiMessage)
        }
    }
}
