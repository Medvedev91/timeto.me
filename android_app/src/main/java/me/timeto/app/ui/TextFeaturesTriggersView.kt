package me.timeto.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.c
import me.timeto.app.squircleShape
import me.timeto.app.toColor
import me.timeto.shared.TextFeatures

@Composable
fun TextFeaturesTriggersView(
    triggers: List<TextFeatures.Trigger>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {

    if (triggers.isEmpty())
        return

    val itemHeight = 26.dp
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        itemsIndexed(
            items = triggers,
            key = { _, checklist -> checklist.id },
        ) { _, trigger ->
            val isLast = triggers.last() == trigger
            HStack(
                modifier = Modifier
                    .padding(end = if (isLast) 0.dp else 8.dp)
                    .height(itemHeight)
                    .clip(squircleShape)
                    .background(trigger.color.toColor())
                    .clickable {
                        trigger.performUI()
                    }
                    .padding(start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = trigger.title,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = c.white,
                )
            }
        }
    }
}
