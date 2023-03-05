package app.time_to.timeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.time_to.timeto.R
import app.time_to.timeto.rememberVM
import kotlinx.coroutines.delay
import timeto.shared.vm.SearchEmojiSheetVM

@Composable
fun SearchEmojiSheet(
    layer: WrapperView__LayerData,
    onSelectEmoji: (String) -> Unit
) {
    // Outside TimetoSheet to have data at open time
    val (vm, state) = rememberVM { SearchEmojiSheetVM() }

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(c.bgFormSheet)
    ) {

        Row(
            modifier = Modifier
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
                modifier = Modifier
                    .weight(1f),
            ) {
                Box(
                    contentAlignment = Alignment.CenterEnd
                ) {

                    BasicTextField__VMState(
                        text = state.inputValue,
                        onValueChange = {
                            vm.setInputValue(it)
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colors.primary),
                        textStyle = LocalTextStyle.current.copy(
                            color = MaterialTheme.colors.onSurface,
                            fontSize = 16.sp
                        ),
                        decorationBox = { innerTextField ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Icon(
                                    painterResource(id = R.drawable.sf_magnifyingglass_medium_medium),
                                    "Search",
                                    tint = c.textSecondary.copy(alpha = 0.3f),
                                    modifier = Modifier
                                        .padding(start = 6.dp, end = 3.dp)
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                        .padding(8.dp)
                                        .offset(y = 0.5.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .sizeIn(minHeight = 40.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    if (state.inputValue.isEmpty()) Text(
                                        state.inputPlaceholder,
                                        style = LocalTextStyle.current.copy(
                                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                            fontSize = 16.sp
                                        )
                                    )
                                    innerTextField()
                                }
                            }
                        },
                        modifier = Modifier
                            .focusRequester(focusRequester)
                    )

                    TextFieldClearButtonView(text = state.inputValue) {
                        focusRequester.requestFocus()
                        vm.setInputValue("")
                    }
                }
            }

            Text(
                "Cancel",
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .clickable { layer.close() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = c.blue,
                fontSize = 17.sp,
            )

            LaunchedEffect(Unit) {
                delay(100)
                focusRequester.requestFocus()
            }
        }

        MyListView__ItemView(
            isFirst = true,
            isLast = true,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxSize()
                // https://google.github.io/accompanist/insets/#inset-consumption
                .navigationBarsPadding()
                .imePadding(),
        ) {

            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    bottom = 6.dp,
                    top = 6.dp,
                    start = 6.dp,
                    end = 6.dp
                )
            ) {
                itemsIndexed(state.selectedEmojis) { _, emoji ->
                    Text(
                        emoji.emoji,
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectEmoji(emoji.emoji)
                                layer.close()
                            },
                        textAlign = TextAlign.Center,
                        fontSize = 28.sp
                    )
                }
            }
        }
    }
}
