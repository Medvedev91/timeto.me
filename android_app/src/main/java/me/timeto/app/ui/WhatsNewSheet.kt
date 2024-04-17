package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.timeto.app.H_PADDING
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.rememberVM
import me.timeto.shared.vm.WhatsNewVm

@Composable
fun WhatsNewSheet(
    layer: WrapperView.Layer,
) {

    val (_, state) = rememberVM {
        WhatsNewVm()
    }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg),
    ) {

        val scrollState = rememberScrollState()

        Sheet__HeaderView(
            title = state.headerTitle,
            scrollState = scrollState,
            bgColor = c.sheetBg,
        )

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .weight(1f),
        ) {

            state.historyItemsUi.forEach { historyItem ->

                VStack(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .padding(horizontal = H_PADDING),
                ) {

                    Text(
                        text = historyItem.title,
                        color = c.text,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = historyItem.text,
                        modifier = Modifier
                            .padding(top = 2.dp),
                        color = c.text,
                    )

                    if (state.historyItemsUi.last() != historyItem)
                        SheetDividerBg(Modifier.padding(top = 12.dp))
                }
            }
        }

        Sheet__BottomViewClose {
            layer.close()
        }
    }
}
