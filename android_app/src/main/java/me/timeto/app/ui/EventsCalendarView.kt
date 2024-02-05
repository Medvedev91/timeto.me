package me.timeto.app.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.timeto.app.VStack
import me.timeto.app.c

@Composable
fun EventsCalendarView(
    modifier: Modifier,
) {

    VStack(
        modifier = modifier,
    ) {

        Text("todo", color = c.white)
    }
}
