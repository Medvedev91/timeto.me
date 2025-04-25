package me.timeto.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.MainActivity
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.roundedShape
import me.timeto.app.ui.Screen
import me.timeto.shared.ui.history.HistoryVm

@Composable
fun HistoryFs() {

    val mainActivity = LocalContext.current as MainActivity

    val (vm, state) = rememberVm {
        HistoryVm()
    }

    Screen {

        val scrollState = rememberLazyListState()

        LazyColumn(
            modifier = Modifier
                .weight(1f),
            state = scrollState,
            reverseLayout = true,
            contentPadding = PaddingValues(top = mainActivity.statusBarHeightDp)
        ) {

            state.daysUi.reversed().forEach { dayUi ->

                item(key = "day_${dayUi.unixDay}") {

                    dayUi.intervalsUi.reversed().forEach { intervalUi ->
                        Text(
                            text = intervalUi.text,
                            color = c.white,
                        )
                    }

                    //
                    // Header

                    HStack(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
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
    }
}
