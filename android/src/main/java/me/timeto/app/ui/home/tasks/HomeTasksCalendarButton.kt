package me.timeto.app.ui.home.tasks

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.timeto.app.R
import me.timeto.app.ui.onePx

@Composable
fun HomeTasksCalendarButton(
    color: Color,
    onClick: () -> Unit,
) {
    HomeTasksIconButton(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .padding(end = 2.dp),
        content = {
            Icon(
                painter = painterResource(R.drawable.sf_calendar_medium_light),
                contentDescription = "New Task",
                tint = color,
                modifier = Modifier
                    .size(21.dp),
            )
        },
    )
}
