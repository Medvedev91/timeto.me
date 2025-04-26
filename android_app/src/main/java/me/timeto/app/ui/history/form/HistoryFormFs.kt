package me.timeto.app.ui.history.form

import androidx.compose.runtime.Composable
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.ui.history.form.HistoryFormVm

@Composable
fun HistoryFormFs(
    initIntervalDb: IntervalDb?,
) {

    val (vm, state) = rememberVm {
        HistoryFormVm(
            initIntervalDb = initIntervalDb,
        )
    }

    Screen {
    }
}
