package me.timeto.shared.ui.history.form.time

import me.timeto.shared.db.IntervalDb

sealed class HistoryFormTimeStrategy {

    data class Picker(
        val initTime: Int,
        val onDone: (Int) -> Unit,
    ): HistoryFormTimeStrategy()

    data class Update(
        val intervalDb: IntervalDb,
    ): HistoryFormTimeStrategy()
}
