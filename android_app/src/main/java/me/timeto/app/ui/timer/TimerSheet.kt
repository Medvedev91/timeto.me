package me.timeto.app.ui.timer

import android.widget.NumberPicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.dpToPx
import me.timeto.app.isSdkQPlus
import me.timeto.app.ui.HStack
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.sheet.HeaderSheet
import me.timeto.app.ui.header.sheet.HeaderSheetButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.squircleShape
import me.timeto.shared.vm.timer_picker.TimerPickerVm

@Composable
fun TimerSheet(
    title: String,
    doneTitle: String,
    initSeconds: Int,
    hints: List<Int>,
    onDone: (Int) -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        TimerPickerVm(
            initSeconds = initSeconds,
            hints = hints,
        )
    }

    val pickerItemsUi: List<TimerPickerVm.PickerItemUi> = state.pickerItemsUi
    val formTimeItemIdx: MutableState<Int> = remember {
        mutableIntStateOf(pickerItemsUi.indexOfFirst { it.seconds == initSeconds })
    }

    VStack {

        ZStack(Modifier.weight(1f))

        Screen(
            modifier = Modifier
                .weight(1f),
            bgColor = c.fg,
        ) {

            HeaderSheet(
                title = title,
                doneButton = HeaderSheetButton(
                    text = doneTitle,
                    onClick = {
                        onDone(pickerItemsUi[formTimeItemIdx.value].seconds)
                        navigationLayer.close()
                    },
                ),
                cancelButton = HeaderSheetButton(
                    text = "Cancel",
                    onClick = {
                        navigationLayer.close()
                    },
                ),
            )

            ZStack(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 80.dp),
                contentAlignment = Alignment.Center,
            ) {

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth(),
                    factory = { context ->
                        NumberPicker(context).apply {
                            setOnValueChangedListener { _, _, new ->
                                formTimeItemIdx.value = new
                            }
                            displayedValues = pickerItemsUi.map { it.title }.toTypedArray()
                            if (isSdkQPlus())
                                textSize = dpToPx(18f).toFloat()
                            wrapSelectorWheel = false
                            minValue = 0
                            maxValue = pickerItemsUi.size - 1
                            value = formTimeItemIdx.value // Set last
                        }
                    }
                )
            }

            val hintsUi = state.hintsUi
            HStack(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = if (hintsUi.isEmpty()) 0.dp else 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                hintsUi.forEach { hintUi ->
                    Text(
                        text = hintUi.title,
                        modifier = Modifier
                            .clip(squircleShape)
                            .clickable {
                                onDone(hintUi.timer)
                                navigationLayer.close()
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.SemiBold,
                        color = c.blue,
                    )
                }
            }
        }
    }
}
