package me.timeto.shared.vm.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.TextFeatures
import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventDb
import me.timeto.shared.db.EventTemplateDb
import me.timeto.shared.launchExIo
import me.timeto.shared.DaytimeUi
import me.timeto.shared.textFeatures
import me.timeto.shared.toHms
import me.timeto.shared.DialogsManager
import me.timeto.shared.UiException
import me.timeto.shared.vm.__Vm

class EventFormVm(
    val initEventDb: EventDb?,
    initText: String?,
    initTime: Int?,
) : __Vm<EventFormVm.State>() {

    data class State(
        val doneText: String,
        val textFeatures: TextFeatures,
        val unixDay: Int,
        val daytimeUi: DaytimeUi,
    ) {

        val selectedUnixTime: UnixTime = UnixTime.byLocalDay(unixDay).inSeconds(
            daytimeUi.hour * 3_600 + daytimeUi.minute * 60
        )

        val text: String = textFeatures.textNoFeatures
        val textPlaceholder = "Event"
        val minTime: Int = UnixTime().localDayStartTime()

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
        val textFeatures: TextFeatures =
            (initEventDb?.text ?: initText ?: "").textFeatures()
        val time: Int =
            initEventDb?.getLocalTime()?.time ?: initTime ?: UnixTime().localDayStartTime()
        val unixTime = UnixTime(time)
        val (h, m, _) =
            (unixTime.time - unixTime.localDayStartTime()).toHms()
        state = MutableStateFlow(
            State(
                doneText = if (initEventDb != null) "Save" else "Create",
                textFeatures = textFeatures,
                unixDay = unixTime.localDay,
                daytimeUi = DaytimeUi(hour = h, minute = m),
            )
        )
    }

    fun setDaytime(daytimeUi: DaytimeUi) {
        state.update { it.copy(daytimeUi = daytimeUi) }
    }

    fun setUnixDay(unixDay: Int) {
        state.update { it.copy(unixDay = unixDay) }
    }

    fun setText(text: String) {
        state.update {
            it.copy(textFeatures = it.textFeatures.copy(textNoFeatures = text))
        }
    }

    fun setTemplate(eventTemplateDb: EventTemplateDb): String {
        val tf: TextFeatures = eventTemplateDb.text.textFeatures()
        val text: String = tf.textNoFeatures
        state.update {
            it.copy(
                textFeatures = tf,
                daytimeUi = DaytimeUi.byDaytime(eventTemplateDb.daytime),
            )
        }
        return text
    }

    fun save(
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit
    ): Unit = launchExIo {
        try {
            // todo check if a text without features
            val textWithFeatures: String =
                state.value.textFeatures.textWithFeatures()
            val time: Int =
                state.value.selectedUnixTime.time

            val eventDb: EventDb? = initEventDb
            if (eventDb != null) {
                eventDb.updateWithValidation(
                    text = textWithFeatures,
                    localTime = time,
                )
            } else {
                EventDb.insertWithValidation(
                    text = textWithFeatures,
                    localTime = time,
                )
            }
            onUi {
                onSuccess()
            }
            EventDb.syncTodaySafe(UnixTime().localDay)
        } catch (e: UiException) {
            dialogsManager.alert(e.uiMessage)
        }
    }

    fun delete(
        eventDb: EventDb,
        dialogsManager: DialogsManager,
        onSuccess: () -> Unit,
    ) {
        val text: String = eventDb.text.textFeatures().textNoFeatures
        dialogsManager.confirmation(
            message = "Are you sure you want to delete \"$text\" event?",
            buttonText = "Delete",
            onConfirm = {
                launchExIo {
                    eventDb.delete()
                    onUi {
                        onSuccess()
                    }
                }
            },
        )
    }
}
