package me.timeto.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import me.timeto.app.HStack
import me.timeto.app.c
import me.timeto.app.roundedShape
import me.timeto.shared.db.ActivityModel__Data.TimerHints.TimerHintUI

@Composable
fun TimerHintsView(
    modifier: Modifier,
    timerHintsUI: List<TimerHintUI>,
    hintHPadding: Dp,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    onStart: () -> Unit,
) {
    HStack(modifier = modifier) {
        timerHintsUI.forEach { hintUI ->
            Text(
                text = hintUI.text,
                modifier = Modifier
                    .clip(roundedShape)
                    .align(Alignment.CenterVertically)
                    .clickable {
                        hintUI.startInterval {
                            onStart()
                        }
                    }
                    .padding(start = hintHPadding, end = hintHPadding, top = 3.dp, bottom = 4.dp),
                color = c.blue,
                fontSize = fontSize,
                fontWeight = fontWeight,
            )
        }
    }
}
