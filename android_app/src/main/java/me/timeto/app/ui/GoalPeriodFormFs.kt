package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.shared.db.GoalDb
import me.timeto.shared.vm.GoalPeriodFormVm

@Composable
fun GoalPeriodFormFs(
    _layer: WrapperView.Layer,
    _initPeriod: GoalDb.Period?,
    _onSelect: (GoalDb.Period) -> Unit,
) {

    val (vm, state) = rememberVm(_initPeriod) {
        GoalPeriodFormVm(_initPeriod)
    }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bg),
    ) {

        val scrollState = rememberScrollState()

        Fs__HeaderAction(
            title = state.headerTitle,
            actionText = state.headerDoneText,
            onCancel = { _layer.close() },
            scrollState = scrollState,
            onDone = {
                vm.buildPeriod { period ->
                    _onSelect(period)
                    _layer.close()
                }
            },
        )

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
        }
    }
}
