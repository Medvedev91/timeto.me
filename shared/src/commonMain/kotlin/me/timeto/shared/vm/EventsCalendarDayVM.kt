package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.UnixTime

class EventsCalendarDayVM(
    val unixDay: Int,
) : __VM<EventsCalendarDayVM.State>() {

    data class State(
        val inNote: String,
    )

    override val state = MutableStateFlow(
        State(
            inNote = run {
                val today = UnixTime().localDay
                val diff = unixDay - today
                if (diff == 0)
                    "Today"
                else if (diff == 1)
                    "Tomorrow"
                else if (diff > 1)
                    "In $diff days"
                else if (diff == -1)
                    "Yesterday"
                else // < -1
                    "${diff * -1} days ago"
            },
        )
    )

    override fun onAppear() {
    }
}
