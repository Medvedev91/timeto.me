package me.timeto.shared.vm.events.templates

import me.timeto.shared.UnixTime
import me.timeto.shared.db.EventTemplateDb
import me.timeto.shared.textFeatures

data class EventTemplateUi(
    val eventTemplateDb: EventTemplateDb,
) {

    val shortText: String =
        eventTemplateDb.text.textFeatures().textNoFeatures.let {
            if (it.length <= 17) it else it.substring(0..14) + ".."
        }

    val timeForEventForm: Int =
        UnixTime().localDayStartTime() + eventTemplateDb.daytime
}
