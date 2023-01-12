package app.time_to.timeto.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timeto.shared.db.ActivityModel

@Composable
fun ActivityEmojiPickerView(
    text: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onSelect: (emoji: String, newString: String) -> Unit,
) {
    val allActivities = ActivityModel.getAscSortedFlow().collectAsState(listOf()).value

    LazyRow(
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(
            allActivities,
            key = { it.id }
        ) { activity ->
            Text(
                activity.emoji,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .clickable {
                        val textFirstEmoji = allActivities
                            .map { it.emoji to text.indexOf(it.emoji) }
                            .filter { it.second != -1 }
                            .minByOrNull { it.second }
                            ?.first

                        val newText = if (textFirstEmoji != null)
                            text.replace(textFirstEmoji, activity.emoji)
                        else
                            "${text.trim()} ${activity.emoji}"

                        onSelect(activity.emoji, newText)
                    }
                    .padding(5.dp),
                fontSize = 24.sp,
            )
        }
    }
}
