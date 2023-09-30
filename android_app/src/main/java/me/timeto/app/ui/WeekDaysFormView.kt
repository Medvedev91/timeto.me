package me.timeto.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.c
import me.timeto.app.roundedShape
import me.timeto.shared.vm.ui.WeekDaysFormUI

@Composable
fun WeekDaysFormView(
    weekDays: List<Int>,
    size: Dp,
    modifier: Modifier,
    onChange: (List<Int>) -> Unit,
) {
    val formUI = WeekDaysFormUI(weekDays)
    HStack(
        modifier = modifier
    ) {
        formUI.weekDaysUI.forEach { weekDayUI ->
            val isSelected = weekDayUI.isSelected
            val bgColor = animateColorAsState(if (isSelected) c.blue else c.sheetBg)
            Text(
                weekDayUI.title,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(size)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) c.blue else c.text,
                        shape = roundedShape
                    )
                    .clip(roundedShape)
                    .background(bgColor.value)
                    .clickable {
                        onChange(formUI.toggleWeekDay(weekDayUI.idx))
                    }
                    .wrapContentHeight(), // To center vertical
                fontWeight = FontWeight.W500,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = if (isSelected) c.white else c.text,
            )
        }
    }
}
