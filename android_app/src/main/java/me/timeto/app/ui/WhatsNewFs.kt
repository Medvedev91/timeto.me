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
import me.timeto.app.*
import me.timeto.shared.vm.WhatsNewVm

@Composable
fun WhatsNewFs(
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

            state.historyItemsUi.forEach { historyItemUi ->

                VStack(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .padding(horizontal = H_PADDING),
                ) {

                    HStack {

                        Text(
                            text = historyItemUi.title,
                            modifier = Modifier
                                .weight(1f),
                            color = c.text,
                            fontWeight = FontWeight.Bold,
                        )

                        Text(
                            text = historyItemUi.timeAgoText,
                            color = c.text,
                        )
                    }

                    Text(
                        text = historyItemUi.text,
                        modifier = Modifier
                            .padding(top = 2.dp),
                        color = c.text,
                    )

                    if (state.historyItemsUi.last() != historyItemUi)
                        SheetDividerBg(Modifier.padding(top = 12.dp))
                }
            }
        }

        Sheet__BottomViewClose {
            layer.close()
        }
    }
}
