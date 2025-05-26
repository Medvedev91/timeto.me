package me.timeto.app.ui.emoji

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.R
import me.timeto.app.ui.ZStack
import me.timeto.app.c
import me.timeto.app.ui.halfDpCeil
import me.timeto.app.ui.halfDpFloor
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.squircleShape
import me.timeto.app.ui.Screen
import me.timeto.app.ui.header.views.HeaderView
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.ui.emoji.EmojiPickerVm

@Composable
fun EmojiPickerFs(
    onDone: (String) -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        EmojiPickerVm()
    }

    Screen(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding(),
    ) {

        val scrollState = rememberLazyGridState()

        HeaderView(
            scrollState = scrollState,
        ) {

            HStack(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val textField = remember {
                    mutableStateOf(TextFieldValue("", TextRange(0)))
                }

                HStack(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                        .clip(squircleShape)
                        .background(c.fg)
                        .padding(top = halfDpCeil),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.sf_magnifyingglass_medium_medium),
                        contentDescription = "Search",
                        tint = c.tertiaryText,
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .size(16.dp)
                            .offset(y = -halfDpFloor),
                    )

                    BasicTextField(
                        value = textField.value,
                        onValueChange = { newValue ->
                            textField.value = newValue
                            vm.search(text = newValue.text)
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done,
                        ),
                        cursorBrush = SolidColor(c.blue),
                        textStyle = LocalTextStyle.current.copy(
                            color = c.text,
                            fontSize = 16.sp,
                        ),
                        decorationBox = { innerTextField ->
                            ZStack(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .sizeIn(minHeight = 36.dp)
                                    .padding(
                                        start = 8.dp,
                                        end = H_PADDING + 16.dp,
                                    ),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (textField.value.text.isEmpty()) {
                                    Text(
                                        text = state.searchPlaceholder,
                                        color = c.tertiaryText,
                                        fontSize = 16.sp,
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )
                }

                Text(
                    text = "Cancel",
                    modifier = Modifier
                        .padding(start = 4.dp, end = 4.dp)
                        .clip(squircleShape)
                        .clickable {
                            navigationLayer.close()
                        }
                        .padding(
                            horizontal = H_PADDING_HALF,
                            vertical = 4.dp,
                        ),
                    color = c.blue,
                )
            }
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
                                onDone(emoji.emoji)
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
