package me.timeto.shared.vm.activities.timer

import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb

sealed class ActivityTimerStrategy {

    data object Simple : ActivityTimerStrategy()

    class Task(
        val taskDb: TaskDb,
    ) : ActivityTimerStrategy()

    class Interval(
        val intervalDb: IntervalDb,
    ) : ActivityTimerStrategy()
}
