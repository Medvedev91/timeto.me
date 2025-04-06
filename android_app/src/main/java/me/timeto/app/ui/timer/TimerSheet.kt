package me.timeto.app.ui.timer

import android.widget.NumberPicker
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import me.timeto.app.VStack
import me.timeto.app.ZStack
import me.timeto.app.dpToPx
import me.timeto.app.isSDKQPlus
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.sheet.HeaderSheet
import me.timeto.app.ui.header.sheet.HeaderSheetButton
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.timer.TimerVm

@Composable
fun TimerSheet(
    title: String,
    doneTitle: String,
    initSeconds: Int,
    onDone: (Int) -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (vm, _) = rememberVm {
        TimerVm(
            initSeconds = initSeconds,
        )
    }

    val pickerItems: List<TimerVm.PickerItem> = vm.pickerItems
    val formTimeItemIdx: MutableState<Int> = remember {
        mutableStateOf(pickerItems.indexOfFirst { it.seconds == initSeconds })
    }

    VStack {

        ZStack(Modifier.weight(1f))

        Screen(
            modifier = Modifier
                .weight(1f),
        ) {

            HeaderSheet(
                title = title,
                doneButton = HeaderSheetButton(
                    text = doneTitle,
                    onClick = {
                        onDone(pickerItems[formTimeItemIdx.value].seconds)
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
                    .padding(horizontal = 80.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {

                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        NumberPicker(context).apply {
                            setOnValueChangedListener { _, _, new ->
                                formTimeItemIdx.value = new
                            }
                            displayedValues = pickerItems.map { it.title }.toTypedArray()
                            if (isSDKQPlus())
                                textSize = dpToPx(18f).toFloat()
                            wrapSelectorWheel = false
                            minValue = 0
                            maxValue = pickerItems.size - 1
                            value = formTimeItemIdx.value // Set last
                        }
                    }
                )
            }
        }
    }
}
