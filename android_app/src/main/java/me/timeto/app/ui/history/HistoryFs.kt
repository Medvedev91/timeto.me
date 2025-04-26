package me.timeto.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.H_PADDING
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.MainActivity
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.limitMin
import me.timeto.app.rememberVm
import me.timeto.app.roundedShape
import me.timeto.app.squircleShape
import me.timeto.app.toColor
import me.timeto.app.ui.Screen
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.footer.Footer
import me.timeto.app.ui.footer.FooterAddButton
import me.timeto.app.ui.footer.FooterPlainButton
import me.timeto.app.ui.history.form.HistoryFormFs
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.history.HistoryVm

private const val barPxSecondsRatio: Int = 60

@Composable
fun HistoryFs() {

    val mainActivity = LocalContext.current as MainActivity
    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        HistoryVm()
    }

    Screen {

        val scrollState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            state = scrollState,
            reverseLayout = true,
            contentPadding = PaddingValues(
                top = mainActivity.statusBarHeightDp,
                bottom = 12.dp,
            ),
        ) {

            state.daysUi.reversed().forEach { dayUi ->

                item(key = "day_${dayUi.unixDay}") {

                    dayUi.intervalsUi.reversed().forEach { intervalUi ->

                        val timeHeight = (intervalUi.secondsForBar / barPxSecondsRatio).dp

                        HStack(
                            modifier = Modifier
                                .padding(horizontal = H_PADDING_HALF)
                                .clip(squircleShape)
                                .clickable {
                                    navigationFs.push {
                                        HistoryFormFs(
                                            initIntervalDb = intervalUi.intervalDb,
                                        )
                                    }
                                },
                        ) {

                            Text(
                                text = intervalUi.timeString,
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .padding(start = H_PADDING_HALF, end = H_PADDING),
                                color = if (intervalUi.isStartsPrevDay) c.transparent else c.text,
                                fontWeight = FontWeight.Medium,
                            )

                            ZStack(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .width(10.dp)
                                    .height(10.dp.limitMin(timeHeight))
                                    .align(Alignment.CenterVertically)
                                    .clip(roundedShape)
                                    .background(intervalUi.color.toColor())
                            )

                            Text(
                                text = intervalUi.text,
                                modifier = Modifier
                                    .padding(start = H_PADDING)
                                    .weight(1f)
                                    .padding(vertical = 2.dp),
                                color = c.white,
                                fontWeight = FontWeight.Medium,
                            )

                            Text(
                                text = intervalUi.periodString,
                                modifier = Modifier
                                    .padding(horizontal = H_PADDING)
                                    .padding(vertical = 2.dp),
                                color = c.textSecondary,
                                fontWeight = FontWeight.Light,
                            )
                        }
                    }

                    //
                    // Header

                    HStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = dayUi.dayText,
                            modifier = Modifier
                                .clip(roundedShape)
                                .background(c.blue)
                                .padding(horizontal = 9.dp)
                                .padding(top = 2.dp, bottom = 1.dp),
                            color = c.white,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }

        Footer(
            scrollState = scrollState,
            isScrollReversed = true,
            contentModifier = Modifier
                .padding(horizontal = H_PADDING_HALF),
        ) {

            FooterAddButton(
                text = "New Entry",
                onClick = {
                    navigationFs.push {
                        HistoryFormFs(
                            initIntervalDb = null,
                        )
                    }
                },
            )

            SpacerW1()

            FooterPlainButton(
                text = "Close",
                color = c.blue,
                onClick = {
                    navigationLayer.close()
                },
            )
        }
    }
}
