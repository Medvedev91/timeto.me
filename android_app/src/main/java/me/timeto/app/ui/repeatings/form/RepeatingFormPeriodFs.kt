package me.timeto.app.ui.repeatings.form

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.padding.FormPaddingBottom
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.navigation.picker.NavigationPickerItem
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.ui.repeatings.form.RepeatingFormPeriodVm

@Composable
fun RepeatingFormPeriodFs(
    initPeriod: RepeatingDb.Period?,
    onDone: (RepeatingDb.Period) -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        RepeatingFormPeriodVm(
            initPeriod = initPeriod,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    vm.buildSelectedPeriod(
                        dialogsManager = navigationFs,
                        onSuccess = { newPeriod ->
                            onDone(newPeriod)
                        }
                    )
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
        ) {

            item {

                FormPaddingTop()

                FormButton(
                    title = "Type",
                    isFirst = true,
                    isLast = true,
                    note = state.periodNote,
                    withArrow = true,
                    onClick = {
                        navigationFs.picker(
                            items = state.periodPickerItemsUi.map { periodUi ->
                                NavigationPickerItem(
                                    title = periodUi.title,
                                    isSelected = periodUi.idx == state.activePeriodIdx,
                                    item = periodUi.idx,
                                )
                            },
                            onDone = { pickerItem ->
                                vm.setActivePeriodIdx(pickerItem.item)
                            },
                        )
                    },
                )

                FormPaddingSectionSection()

                FormPaddingBottom(withNavigation = true)
            }
        }
    }
}
