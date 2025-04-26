package me.timeto.app.ui.history.form

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.activities.ActivityPickerFs
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.IntervalDb
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
            }
        }
    }
}
