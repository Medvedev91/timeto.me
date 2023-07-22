package me.timeto.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import me.timeto.app.HStack
import me.timeto.shared.TextFeatures

private val triggerShape = RoundedCornerShape(1.dp)

@Composable
fun TriggersListIconsView(
    triggers: List<TextFeatures.Trigger>,
    fontSize: TextUnit,
) {
    if (triggers.isEmpty())
        return

    HStack {
        triggers.forEach { trigger ->
            Text(
                text = trigger.emoji,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .clip(triggerShape)
                    .clickable {
                        trigger.performUI()
                    },
                fontSize = fontSize,
            )
        }
    }
}
