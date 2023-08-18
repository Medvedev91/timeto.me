package me.timeto.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.c
import me.timeto.app.roundedShape
import me.timeto.shared.ui.TimerHintUI

@Composable
fun TimerHintsView(
    timerHintsUI: List<TimerHintUI>,
    hintHPadding: Dp,
    fontSize: TextUnit,
    fontWeight: FontWeight,
    onStart: () -> Unit,
) {
    HStack {
        timerHintsUI.forEach { hintUI ->
            val isPrimary = hintUI.isPrimary
            val hPadding = if (isPrimary) 6.dp else hintHPadding
            Text(
                text = hintUI.text,
                modifier = Modifier
                    .padding(start = if (isPrimary) 2.dp else 0.dp)
                    .clip(roundedShape)
                    .align(Alignment.CenterVertically)
                    .background(if (isPrimary) c.blue else c.transparent)
                    .clickable {
                        hintUI.startInterval {
                            onStart()
                        }
                    }
                    .padding(start = hPadding, end = hPadding, top = 3.dp, bottom = 4.dp),
                color = if (isPrimary) c.white else c.blue,
                fontSize = if (isPrimary) (fontSize.value - 1).sp else fontSize,
                fontWeight = if (isPrimary) FontWeight.Medium else fontWeight,
            )
        }
    }
}
