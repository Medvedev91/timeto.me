package app.time_to.timeto.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.rememberVM
import timeto.shared.vm.ActivityEmojiPickerVM

@Composable
fun ActivityEmojiPickerView(
    text: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onSelect: (newString: String) -> Unit,
) {
    val (vm, state) = rememberVM { ActivityEmojiPickerVM() }
    LazyRow(
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(
            state.activitiesUI,
            key = { it.activity.id }
        ) { activityUI ->
            Text(
                activityUI.activity.emoji,
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .clickable {
                        onSelect(vm.upText(text, activityUI.activity))
                    }
                    .padding(5.dp),
                fontSize = 24.sp,
            )
        }
    }
}
