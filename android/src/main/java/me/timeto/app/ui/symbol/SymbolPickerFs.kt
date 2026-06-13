package me.timeto.app.ui.symbol

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.ui.Screen
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.emoji.EmojiPickerFs
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.squircleShape
import me.timeto.shared.Symbol
import me.timeto.shared.vm.symbol.SymbolPickerVm

private val rowHeight = 40.dp

@Composable
fun SymbolPickerFs(
    onPick: (Symbol) -> Unit,
) {
    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (_, state) = rememberVm {
        SymbolPickerVm()
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = "Icon",
            scrollState = scrollState,
            actionButton = null,
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LazyColumn(
            state = scrollState,
        ) {

            item {
                PlainButton(
                    text = "Emoji",
                    onClick = {
                        navigationFs.push {
                            EmojiPickerFs(
                                onDone = { emoji ->
                                    onPick(Symbol.Emoji(emoji))
                                    navigationLayer.close()
                                },
                            )
                        }
                    },
                )
            }

            item {
                PlainButton(
                    text = "Symbol",
                    onClick = {
                        navigationFs.push {
                            SymbolLetterPickerFs(
                                initText = "",
                                onPick = { symbolLetter ->
                                    onPick(symbolLetter)
                                    navigationLayer.close()
                                },
                            )
                        }
                    },
                )
            }

            state.symbolChunks.forEach { symbolsRow ->
                item {
                    HStack(
                        modifier = Modifier
                            .height(rowHeight),
                    ) {
                        symbolsRow.forEach { symbol ->
                            ZStack(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f)
                                    .clip(squircleShape)
                                    .clickable {
                                        onPick(symbol)
                                        navigationLayer.close()
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                SymbolView(
                                    symbol = symbol,
                                    color = c.white,
                                    letterSize = 23.sp, // No matter
                                    iconSize = 23.dp,
                                    emojiSize = 18.sp, // No matter
                                    modifier = Modifier,
                                )
                            }
                        }
                    }
                }
            }

            item {
                ZStack(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun PlainButton(
    text: String,
    onClick: () -> Unit,
) {
    ZStack(
        modifier = Modifier
            .height(rowHeight)
            .padding(horizontal = H_PADDING_HALF)
            .clip(squircleShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = H_PADDING_HALF),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = text,
            color = c.blue,
        )
    }
}

