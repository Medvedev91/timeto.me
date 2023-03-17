package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.R
import app.time_to.timeto.toColor
import timeto.shared.Trigger

@Composable
fun TriggersView__ListView(
    triggers: List<Trigger>,
    withOnClick: Boolean,
    modifier: Modifier = Modifier,
    withDeletion: ((trigger: Trigger) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues()
) {
    if (triggers.isEmpty())
        return

    val itemHeight = 26.dp
    LazyRow(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        itemsIndexed(
            triggers,
            key = { _, checklist -> checklist.id }
        ) { _, trigger ->
            val isLast = triggers.last() == trigger
            Row(
                modifier = Modifier
                    .padding(end = if (isLast) 0.dp else 8.dp)
                    .height(itemHeight)
                    .clip(MySquircleShape(len = 50f))
                    .background(
                        trigger
                            .getColor()
                            .toColor()
                    )
                    .clickable(withOnClick) {
                        trigger.performUI()
                    }
                    .padding(start = 8.dp, end = if (withDeletion != null) 1.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    trigger.title,
                    modifier = Modifier
                        .offset(y = (-0.8).dp),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = c.white
                )
                if (withDeletion != null)
                    Icon(
                        painterResource(id = R.drawable.ic_round_close_24),
                        "Delete",
                        modifier = Modifier
                            .size(itemHeight)
                            .clip(RoundedCornerShape(99.dp))
                            .clickable {
                                withDeletion(trigger)
                            }
                            .padding(4.dp),
                        tint = c.white
                    )
            }
        }
    }
}
