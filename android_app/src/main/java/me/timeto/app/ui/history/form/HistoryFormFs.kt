package me.timeto.app.ui.history.form

import android.widget.NumberPicker
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.c
import me.timeto.app.dpToPx
import me.timeto.app.isSDKQPlus
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.activities.ActivityPickerFs
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterPlainButton
import me.timeto.app.ui.form.FormItemView
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.ui.history.form.HistoryFormUtils
import me.timeto.shared.ui.history.form.HistoryFormVm

@Composable
fun HistoryFormFs(
    initIntervalDb: IntervalDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        HistoryFormVm(
            initIntervalDb = initIntervalDb,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.saveText,
                isEnabled = true,
                onClick = {
                    vm.save(
                        dialogsManager = navigationFs,
                        onSuccess = {
                            navigationLayer.close()
                        },
                    )
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
            state = scrollState,
            modifier = Modifier
                .weight(1f),
        ) {

            item {

                FormPaddingTop()

                FormButton(
                    title = state.activityTitle,
                    isFirst = true,
                    isLast = false,
                    note = state.activityNote,
                    noteColor =
                        if (state.activityDb == null) c.red
                        else c.textSecondary,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            ActivityPickerFs(
                                initActivityDb = state.activityDb,
                                onDone = { newActivityDb ->
                                    vm.setActivityDb(newActivityDb)
                                },
                            )
                        }
                    },
                )

                val timerItemsUi = state.timerItemsUi
                val formTimeItemIdx: MutableState<Int> = remember {
                    mutableStateOf(timerItemsUi.indexOfFirst { it.time == state.time })
                }

                val formTimeItemIdxValue: Int = formTimeItemIdx.value
                LaunchedEffect(formTimeItemIdxValue) {
                    vm.setTime(timerItemsUi[formTimeItemIdxValue].time)
                }

                FormItemView(
                    isFirst = false,
                    isLast = true,
                    modifier = Modifier,
                    content = {
                        AndroidView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 80.dp),
                            factory = { context ->
                                NumberPicker(context).apply {
                                    setOnValueChangedListener { _, _, new ->
                                        formTimeItemIdx.value = new
                                    }
                                    displayedValues = timerItemsUi.map { it.title }.toTypedArray()
                                    if (isSDKQPlus())
                                        textSize = dpToPx(18f).toFloat()
                                    wrapSelectorWheel = false
                                    minValue = 0
                                    maxValue = timerItemsUi.size - 1
                                    value = formTimeItemIdx.value // Set last
                                }
                            }
                        )
                    },
                )
            }
        }

        val intervalDb: IntervalDb? = state.initIntervalDb
        if (intervalDb != null) {

            Footer(
                scrollState = scrollState,
                contentModifier = Modifier
                    .padding(horizontal = H_PADDING_HALF),
            ) {

                FooterPlainButton(
                    text = "Delete",
                    onClick = {
                        vm.delete(
                            intervalDb = intervalDb,
                            dialogsManager = navigationFs,
                            onSuccess = {
                                navigationLayer.close()
                            },
                        )
                    },
                )

                SpacerW1()

                FooterPlainButton(
                    text = HistoryFormUtils.moveToTasksTitle,
                    onClick = {
                        vm.moveToTasks(
                            intervalDb = intervalDb,
                            dialogsManager = navigationFs,
                            onSuccess = {
                                navigationLayer.close()
                            },
                        )
                    },
                )
            }
        }
    }
}
