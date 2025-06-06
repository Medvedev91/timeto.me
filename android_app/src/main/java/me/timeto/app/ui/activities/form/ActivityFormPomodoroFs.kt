package me.timeto.app.ui.activities.form

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.plain.FormPlainButtonSelection
import me.timeto.app.ui.form.plain.FormPlainPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.vm.activities.form.ActivityFormVm

@Composable
fun ActivityFormPomodoroFs(
    vm: ActivityFormVm,
    state: ActivityFormVm.State,
) {

    val navigationLayer = LocalNavigationLayer.current

    Screen(
        modifier = Modifier
            .navigationBarsPadding(),
    ) {

        val scrollState = rememberLazyListState()

        Header(
            title = state.pomodoroTitle,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    navigationLayer.close()
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
        ) {

            item {
                FormPlainPaddingTop()
            }

            val dayStartListItemsUi = state.pomodoroListItemsUi
            dayStartListItemsUi.forEach { itemUi ->
                item {
                    FormPlainButtonSelection(
                        title = itemUi.text,
                        isSelected = itemUi.isSelected,
                        isFirst = dayStartListItemsUi.first() == itemUi,
                        modifier = Modifier,
                        onClick = {
                            vm.setPomodoroTimer(itemUi.timer)
                            navigationLayer.close()
                        },
                    )
                }
            }
        }
    }
}
