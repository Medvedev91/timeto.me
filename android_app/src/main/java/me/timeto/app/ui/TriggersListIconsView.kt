package me.timeto.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import me.timeto.app.HStack
import me.timeto.shared.TextFeatures

@Composable
fun TriggersListIconsView(
    triggers: List<TextFeatures.Trigger>,
    fontSize: TextUnit,
) {
    if (triggers.isEmpty())
        return

    HStack(
        modifier = Modifier
            .padding(start = 3.dp)
    ) {
        triggers.forEach { trigger ->
            Text(
                text = trigger.emoji,
                modifier = Modifier
                    .padding(horizontal = 3.dp),
                fontSize = fontSize,
            )
        }
    }
}
