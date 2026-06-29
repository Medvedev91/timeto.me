package me.timeto.app.ui.home.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DismissState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.timeto.app.ui.HStack
import me.timeto.app.ui.SpacerW1
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.home.HomeScreen__barTextLineHeight
import me.timeto.app.ui.home.HomeScreen__itemCircleFontWeight
import me.timeto.app.ui.home.HomeScreen__itemHeight
import me.timeto.app.ui.home.HomeScreen__primaryFontSize
import me.timeto.app.ui.roundedShape

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeTaskStaEndView(
    state: DismissState,
    onMoveToTimer: () -> Unit,
    onDelete: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    HStack(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.red),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = "Move to Timer",
            color = c.white,
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(roundedShape)
                .clickable {
                    onMoveToTimer()
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )

        SpacerW1()

        Text(
            text = "Cancel",
            color = c.white,
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(roundedShape)
                .clickable {
                    scope.launch {
                        state.reset()
                    }
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )

        ZStack(
            modifier = Modifier
                .padding(end = 7.dp)
                .height(HomeScreen__itemHeight - 8.dp)
                .clip(roundedShape)
                .background(c.white)
                .padding(horizontal = 10.dp)
                .clickable {
                    onDelete()
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Delete",
                color = c.red,
                fontSize = HomeScreen__primaryFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
                lineHeight = HomeScreen__barTextLineHeight,
            )
        }
    }
}
