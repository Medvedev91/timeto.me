package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.UnixTime
import me.timeto.shared.vm.SummarySheetVM

private val periodHintsHeight = 40.dp
private val periodHintShape = SquircleShape(len = 50f)
private val periodHintBg = c.sheetBg.copy(alpha = 0.8f)

@Composable
fun SummarySheet(
    layer: WrapperView.Layer,
) {

    val (vm, state) = rememberVM { SummarySheetVM() }

    VStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.sheetBg),
    ) {

        ZStack(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {

            HStack(
                modifier = Modifier
                    .height(periodHintsHeight)
                    .align(Alignment.BottomCenter),
            ) {
                state.periodHints.forEach { period ->
                    Text(
                        period.title,
                        modifier = Modifier
                            .clip(periodHintShape)
                            .clickable {
                                vm.setPeriod(period.pickerTimeStart, period.pickerTimeFinish)
                            }
                            .background(periodHintBg)
                            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 7.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = if (period.isActive) FontWeight.Black else FontWeight.Light,
                        color = if (period.isActive) c.white else c.text,
                    )
                }
            }
        }

        Sheet__BottomView {

            HStack(
                modifier = Modifier
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Icon(
                    painter = painterResource(R.drawable.sf_chart_pie_small_thin),
                    contentDescription = "Pie Chart",
                    tint = c.textSecondary,
                    modifier = Modifier
                        .alpha(0.7f)
                        .size(30.dp)
                        .clip(roundedShape)
                        .clickable {
                            layer.close()
                        }
                        .padding(5.dp),
                )

                SpacerW1()

                DateButtonView(
                    text = state.timeStartText,
                    unixTime = state.pickerTimeStart,
                    minTime = state.minPickerTime,
                    maxTime = state.maxPickerTime,
                ) {
                    vm.setPickerTimeStart(it)
                }

                Text(
                    "-",
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 1.dp)
                        .align(Alignment.CenterVertically),
                    fontSize = 14.sp,
                    color = c.text
                )

                DateButtonView(
                    text = state.timeFinishText,
                    unixTime = state.pickerTimeFinish,
                    minTime = state.minPickerTime,
                    maxTime = state.maxPickerTime,
                ) {
                    vm.setPickerTimeFinish(it)
                }

                SpacerW1()

                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = c.textSecondary,
                    modifier = Modifier
                        .alpha(0.7f)
                        .size(30.dp)
                        .clip(roundedShape)
                        .clickable {
                            layer.close()
                        }
                        .padding(4.dp),
                )
            }
        }
    }
}

@Composable
private fun DateButtonView(
    text: String,
    unixTime: UnixTime,
    minTime: UnixTime,
    maxTime: UnixTime,
    onSelect: (UnixTime) -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(squircleShape)
            .background(c.summaryDatePicker)
            .clickable {
                Dialog.showDatePicker(
                    unixTime = unixTime,
                    minTime = minTime,
                    maxTime = maxTime,
                    onSelect = onSelect,
                )
            }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = c.white,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}
