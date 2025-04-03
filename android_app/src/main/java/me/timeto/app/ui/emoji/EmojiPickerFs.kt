package me.timeto.app.ui.emoji

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.views.HeaderView
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.emoji.EmojiPickerVm

@Composable
fun EmojiPickerFs(
    onPick: (String) -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        EmojiPickerVm()
    }

    Screen(
        modifier = Modifier
            .imePadding(),
    ) {

        val scrollState = rememberLazyGridState()

        HeaderView(
            scrollState = scrollState,
        ) {
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            state = scrollState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                bottom = 6.dp,
                top = 6.dp,
                start = 6.dp,
                end = 6.dp,
            )
        ) {
            state.emojis.forEach { emoji ->
                item {
                    Text(
                        text = emoji.emoji,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable {
                                onPick(emoji.emoji)
                                navigationLayer.close()
                            },
                        textAlign = TextAlign.Center,
                        fontSize = 28.sp,
                    )
                }
            }
        }
    }
}
