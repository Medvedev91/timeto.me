package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.timeto.app.*
import me.timeto.shared.vm.ActivityPomodoroSheetVm

@Composable
fun ActivityPomodoroSheet(
    layer: WrapperView.Layer,
    selectedTimer: Int,
    onPick: (Int) -> Unit,
) {

    val (vm, state) = rememberVM(selectedTimer) {
        ActivityPomodoroSheetVm(selectedTimer = selectedTimer)
    }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg)
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderViewOld(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.doneTitle,
            isDoneEnabled = true,
            scrollState = scrollState,
        ) {
            onPick(state.prepSelectedTime())
            layer.close()
        }

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            ZStack(Modifier.height(20.dp)) { }

            val listItemsUi = state.listItemsUi
            listItemsUi.forEach { listItemUi ->
                val isFirst = state.listItemsUi.first() == listItemUi
                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = state.listItemsUi.last() == listItemUi,
                    withTopDivider = !isFirst,
                ) {
                    MyListView__ItemView__RadioView(
                        text = listItemUi.text,
                        isActive = listItemUi.isSelected,
                    ) {
                        vm.setTimer(listItemUi.time)
                    }
                }
            }

            ZStack(Modifier.height(20.dp)) { }
        }
    }
}
