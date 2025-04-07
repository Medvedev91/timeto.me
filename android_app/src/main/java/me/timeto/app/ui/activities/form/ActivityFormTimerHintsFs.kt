package me.timeto.app.ui.activities.form

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.form.FormPlainPaddingTop
import me.timeto.app.ui.form.plain.FormPlainButtonDeletion
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.timer.TimerSheet
import me.timeto.shared.ui.activities.form.ActivityFormTimerHintsVm

private const val timerSheetHeader = "Timer Hint"

@Composable
fun ActivityFormTimerHintsFs(
    initTimerHints: Set<Int>,
    onDone: (Set<Int>) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ActivityFormTimerHintsVm(
            initTimerHints = initTimerHints,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = "Timer Hints",
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    onDone(state.timerHints)
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
                .weight(1f),
            state = scrollState,
        ) {

            item {
                FormPlainPaddingTop()
            }

            state.timerHintsUi.forEachIndexed { idx, timerHintUi ->
                item(key = timerHintUi.seconds) {
                    FormPlainButtonDeletion(
                        title = timerHintUi.text,
                        isFirst = idx == 0,
                        modifier = Modifier
                            .animateItem(),
                        onClick = {
                            navigationFs.push {
                                TimerSheet(
                                    title = timerSheetHeader,
                                    doneTitle = "Save",
                                    initSeconds = timerHintUi.seconds,
                                    onDone = { newSeconds ->
                                        vm.delete(timerHintUi.seconds)
                                        vm.add(newSeconds)
                                    },
                                )
                            }
                        },
                        onDelete = {
                            vm.delete(timerHintUi.seconds)
                        },
                    )
                }
            }
        }

        Footer(
            scrollState = scrollState,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF),
        ) {
            FooterAddButton(
                text = "Timer Hint",
                onClick = {
                    navigationFs.push {
                        TimerSheet(
                            title = timerSheetHeader,
                            doneTitle = "Add",
                            initSeconds = 45 * 60,
                            onDone = { newSeconds ->
                                vm.add(newSeconds)
                            },
                        )
                    }
                },
            )
        }
    }
}
