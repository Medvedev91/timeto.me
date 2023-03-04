package app.time_to.timeto.ui

import android.text.format.DateFormat
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.*
import app.time_to.timeto.R
import timeto.shared.UnixTime
import timeto.shared.vm.ChartVM
import java.util.*

@Composable
fun ChartDialogView(
    onClose: () -> Unit,
) {

    val (vm, state) = rememberVM { ChartVM() }

    Box(
        modifier = Modifier.background(c.background2)
    ) {

        Column {

            Box(
                modifier = Modifier
                    .padding(top = 30.dp, start = 35.dp, end = 35.dp)
                    .align(Alignment.CenterHorizontally)
                    // ChartUI() must be in square
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {

                WyouChart.ChartUI(state.pieItems, state.selectedId) {
                    vm.selectId(it)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    bottom = 100.dp,
                    top = 26.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                itemsIndexed(state.pieItems) { _, pie ->
                    val curId = pie.id
                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Min) // To use fillMaxHeight() inside
                            .padding(start = 2.dp)
                            .clip(MySquircleShape())
                            .clickable {
                                vm.selectId(if (state.selectedId == curId) null else curId)
                            }
                            .padding(start = 6.dp, end = 1.dp, top = 5.dp, bottom = 5.dp)
                    ) {
                        val width = animateDpAsState(
                            if (state.selectedId == curId) 23.dp else 10.dp,
                            spring(stiffness = Spring.StiffnessLow)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(width.value)
                                .clip(RoundedCornerShape(5.dp))
                                .background(pie.color.toColor())
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                        ) {

                            Text(
                                pie.title,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Row {

                                Text(
                                    pie.customData as String,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 1.dp, bottom = 2.dp),
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.W300,
                                    fontSize = 12.sp,
                                )


                                Text(
                                    pie.subtitleTop!!,
                                    modifier = Modifier
                                        .padding(top = 1.dp, bottom = 2.dp),
                                    textAlign = TextAlign.Start,
                                    fontWeight = FontWeight.W300,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(bottom = 22.dp)
                .align(Alignment.BottomCenter)
        ) {

            Row(
                modifier = Modifier
                    .padding(bottom = 12.dp, end = 2.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                state.periodHints.forEach { period ->
                    Text(
                        period.title,
                        modifier = Modifier
                            .clip(MySquircleShape(len = 50f))
                            .clickable {
                                vm.upPeriod(period.dayStart, period.dayFinish)
                            }
                            .background(c.background2.copy(alpha = 0.8f))
                            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 7.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 11.5.sp,
                        fontWeight = if (period.title == state.activePeriodHintTitle) FontWeight.W900 else FontWeight.W300,
                        color = c.text
                    )
                }
            }

            Row {

                Box(modifier = Modifier.weight(1f))

                ChartDatePicker(
                    UnixTime.byLocalDay(state.dayStart),
                    minPickableDay = state.minPickerDay,
                    minSavableDay = state.minPickerDay,
                    maxDay = state.maxPickerDay,
                ) {
                    vm.upDayStart(it.localDay)
                }

                Text(
                    "to",
                    modifier = Modifier
                        .padding(start = 9.dp, end = 8.dp, bottom = 1.dp)
                        .align(Alignment.CenterVertically),
                    fontSize = 14.sp,
                    color = c.text
                )

                ChartDatePicker(
                    UnixTime.byLocalDay(state.dayFinish),
                    minPickableDay = state.minPickerDay,
                    minSavableDay = state.minPickerDay,
                    maxDay = state.maxPickerDay,
                ) {
                    vm.upDayFinish(it.localDay)
                }

                Box(modifier = Modifier.weight(1f)) {
                    Icon(
                        painterResource(id = R.drawable.ic_round_close_24),
                        "Close",
                        tint = c.textSecondary,
                        modifier = Modifier
                            .alpha(0.7f)
                            .align(Alignment.Center)
                            .padding(end = 4.dp)
                            .size(30.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(c.background2)
                            .clickable {
                                onClose()
                            }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartDatePicker(
    defaultTime: UnixTime,
    modifier: Modifier = Modifier,
    minPickableDay: Int,
    minSavableDay: Int,
    maxDay: Int = UnixTime.MAX_DAY,
    withTimeBtnText: String? = null,
    onSelect: (UnixTime) -> Unit,
) {
    val layers = LocalWrapperViewLayers.current
    val calendar = Calendar.getInstance(Locale.ENGLISH)
    calendar.timeInMillis = defaultTime.time * 1_000L

    Surface(
        elevation = 6.dp,
        shape = MySquircleShape(),
        color = c.blue,
        modifier = modifier
            .height(30.dp)
            .clickable {
                MyDialog.showDatePicker(
                    layers = layers,
                    defaultTime = defaultTime,
                    minPickableDay = minPickableDay,
                    minSavableDay = minSavableDay,
                    maxDay = maxDay,
                    withTimeBtnText = withTimeBtnText,
                    onSelect = onSelect
                )
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            val is0000 = defaultTime.localDayStartTime() == defaultTime.time
            val format = if (is0000) "d MMM, E" else "d MMM, E HH:mm"
            Text(
                DateFormat.format(format, calendar).toString(),
                color = c.white,
                modifier = Modifier.padding(horizontal = 10.dp),
                fontSize = 14.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}
