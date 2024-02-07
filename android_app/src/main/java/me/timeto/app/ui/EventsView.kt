package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.shared.vm.EventsVM

private val menuTopPadding = 8.dp
private val menuBottomPadding = menuTopPadding.goldenRatioUp().goldenRatioUp()

@Composable
fun EventsView() {

    val (vm, state) = rememberVM { EventsVM() }

    VStack(
        modifier = Modifier
            .fillMaxSize(),
    ) {

        if (state.isCalendarOrList) {
            EventsCalendarView(
                modifier = Modifier
                    .weight(1f),
            )
        } else {
            EventsListView(
                modifier = Modifier
                    .weight(1f),
            )
        }

        DividerBg(
            modifier = Modifier
                .padding(start = H_PADDING, end = TasksView__PADDING_END),
        )

        HStack(
            modifier = Modifier
                .padding(top = menuTopPadding, bottom = menuBottomPadding),
        ) {

            ModeButton(
                text = "Calendar",
                modifier = Modifier
                    .padding(start = H_PADDING - halfDp),
                isActive = state.isCalendarOrList,
                onClick = {
                    vm.setIsCalendarOrList(true)
                },
            )

            ModeButton(
                text = "List",
                modifier = Modifier
                    .padding(start = 8.dp),
                isActive = !state.isCalendarOrList,
                onClick = {
                    vm.setIsCalendarOrList(false)
                },
            )
        }
    }
}

//
// Mode Button

private val modeButtonShape = SquircleShape(len = 50f)

@Composable
private fun ModeButton(
    text: String,
    modifier: Modifier,
    isActive: Boolean,
    onClick: () -> Unit,
) {

    Text(
        text = text,
        modifier = modifier
            .clip(shape = modeButtonShape)
            .clickable {
                onClick()
            }
            .background(animateColorAsState(if (isActive) c.blue else c.transparent).value)
            .padding(horizontal = 7.dp)
            .padding(top = 2.dp, bottom = 3.dp),
        color = if (isActive) c.white else c.text,
        fontSize = 14.sp,
    )
}
