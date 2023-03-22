package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.time_to.timeto.rememberVM
import timeto.shared.vm.FullScreenSettingsSheetVM

@Composable
fun FullScreenSettingsSheet(
    layer: WrapperView.Layer,
) {

    val (vm, state) = rememberVM { FullScreenSettingsSheetVM() }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bgFormSheet)
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderView(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = "Done",
            isDoneEnabled = true,
            scrollToHeader = scrollState.value,
        ) {
            layer.close()
        }

        Column(
            modifier = Modifier
                .verticalScroll(
                    state = scrollState
                )
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            Row(Modifier.height(20.dp)) { }

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                withTopDivider = false,
            ) {
                MyListView__ItemView__SwitchView(
                    text = state.showTimeOfTheDayTitle,
                    isActive = state.isShowTimeOfTheDay,
                ) {
                    vm.toggleShowTimeOfTheDay()
                }
            }
        }
    }
}
