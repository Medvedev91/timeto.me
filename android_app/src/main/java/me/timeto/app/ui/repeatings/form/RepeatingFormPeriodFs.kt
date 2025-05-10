package me.timeto.app.ui.repeatings.form

import android.widget.NumberPicker
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.H_PADDING
import me.timeto.app.R
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.dpToPx
import me.timeto.app.isSDKQPlus
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.button.FormButtonView
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
                            navigationLayer.close()
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

                when (state.activePeriodIdx) {
                    0 -> {}
                    1 -> {
                        VStack(
                            modifier = Modifier
                                .padding(horizontal = H_PADDING + 4.dp),
                        ) {
                            val days: List<Int> = (2..666).toList()
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                factory = { context ->
                                    NumberPicker(context).apply {
                                        setOnValueChangedListener { _, _, new ->
                                            vm.setSelectedNDays(new)
                                        }
                                        displayedValues = days.map { "$it" }.toTypedArray()
                                        if (isSDKQPlus())
                                            textSize = dpToPx(18f).toFloat()
                                        wrapSelectorWheel = false
                                        minValue = 0
                                        maxValue = days.size - 1
                                        value = days.indexOf(state.selectedNDays) // Set last
                                    }
                                }
                            )
                        }
                    }
                    2 -> {
                        val daysOfWeekUi = state.daysOfWeekUi
                        daysOfWeekUi.forEachIndexed { idx, dayOfWeekUi ->
                            FormButtonView(
                                title = dayOfWeekUi.title,
                                titleColor = null,
                                isFirst = idx == 0,
                                isLast = ((idx + 1) == daysOfWeekUi.size),
                                modifier = Modifier,
                                rightView = {
                                    if (dayOfWeekUi.idx in state.selectedDaysOfWeek) {
                                        Icon(
                                            painterResource(R.drawable.sf_checkmark_medium_medium),
                                            contentDescription = "Selected",
                                            tint = c.white,
                                            modifier = Modifier
                                                .padding(end = H_PADDING)
                                                .size(13.dp),
                                        )
                                    }
                                },
                                onClick = {
                                    vm.toggleDayOfWeek(dayOfWeekUi.idx)
                                },
                                onLongClick = null,
                            )
                        }
                    }
                }

                FormPaddingBottom(withNavigation = true)
            }
        }
    }
}
