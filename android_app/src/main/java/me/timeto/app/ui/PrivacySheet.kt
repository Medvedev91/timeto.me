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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.VStack
import me.timeto.app.c
import me.timeto.app.rememberVM
import me.timeto.shared.vm.PrivacySheetVM

private val hPadding = MyListView.PADDING_OUTER_HORIZONTAL

@Composable
fun PrivacySheet(
    layer: WrapperView.Layer,
) {

    val (_, state) = rememberVM { PrivacySheetVM() }

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

            PView(state.text1, topPadding = 8.dp)

            PView(state.text2)

            PView(state.text3)

            PView(state.text4, fontWeight = FontWeight.Bold)

            PView(state.text5)

            VStack(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .background(c.sheetFg)
                    .padding(top = 4.dp, bottom = 12.dp),
            ) {
                state.sendItems.forEach { sendItem ->
                    PView(sendItem, topPadding = 4.dp)
                }
            }

            PView(state.text6)

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                modifier = Modifier
                    .padding(top = 24.dp),
                withTopDivider = false,
            ) {
                MyListView__ItemView__SwitchView(
                    text = state.sendReportsTitle,
                    isActive = state.isSendReportsEnabled,
                ) {
                }
            }

        }

        Sheet__BottomViewClose {
            layer.close()
        }
    }
}

@Composable
private fun PView(
    text: String,
    topPadding: Dp = 16.dp,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = hPadding)
            .padding(top = topPadding),
        color = c.white,
        lineHeight = 22.sp,
        fontWeight = fontWeight,
    )
}
