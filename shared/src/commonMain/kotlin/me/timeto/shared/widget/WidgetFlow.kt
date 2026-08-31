package me.timeto.shared.widget

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import me.timeto.shared.db.ChecklistItemDb
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.db.TaskDb
import me.timeto.shared.ioScope

object WidgetFlow {

    val flow = MutableSharedFlow<Unit>()

    private var job: Job? = null

    fun startSafe() {
        job?.cancel()
        job = ioScope().launch {
            try {
                combine(
                    ChecklistItemDb.anyChangeFlow(),
                    IntervalDb.anyChangeFlow(),
                    TaskDb.anyChangeFlow(),
                ) { _, _, _ ->
                    flow.emit(Unit)
                }.collect()
            } catch (_: Exception) {
            }
        }
    }
}
