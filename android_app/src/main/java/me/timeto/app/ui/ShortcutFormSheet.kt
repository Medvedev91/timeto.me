package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.*
import me.timeto.app.R
import me.timeto.shared.db.ShortcutModel
import me.timeto.shared.vm.ShortcutFormSheetVM

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShortcutFormSheet(
    layer: WrapperView.Layer,
    editedShortcut: ShortcutModel?,
) {

    val (vm, state) = rememberVM(editedShortcut) { ShortcutFormSheetVM(editedShortcut) }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(c.bgFormSheet)
    ) {

        val scrollState = rememberScrollState()

        Sheet.HeaderView(
            onCancel = { layer.close() },
            title = state.headerTitle,
            doneText = state.headerDoneText,
            isDoneEnabled = state.isHeaderDoneEnabled,
            scrollState = scrollState,
        ) {
            vm.save {
                layer.close()
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(state = scrollState)
                .padding(bottom = 20.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {

            MyListView__Padding__SectionSection()

            MyListView__HeaderView(
                title = state.inputNameHeader,
            )

            MyListView__Padding__HeaderSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.inputNamePlaceholder,
                    text = state.inputNameValue,
                    onTextChanged = { newText -> vm.setInputNameValue(newText) },
                )
            }

            MyListView__HeaderView(
                title = state.inputUriHeader,
                Modifier.padding(top = 30.dp)
            )

            MyListView__Padding__HeaderSection()

            MyListView__ItemView(
                isFirst = true,
                isLast = true,
            ) {
                MyListView__ItemView__TextInputView(
                    placeholder = state.inputUriPlaceholder,
                    text = state.inputUriValue,
                    onTextChanged = { newText -> vm.setInputUriValue(newText) },
                )
            }

            MyListView__HeaderView(
                title = "EXAMPLES",
                Modifier.padding(top = 60.dp)
            )

            MyListView__Padding__HeaderSection()

            shortcutExamples.forEach { example ->
                val isFirst = shortcutExamples.first() == example
                MyListView__ItemView(
                    isFirst = isFirst,
                    isLast = shortcutExamples.last() == example,
                    withTopDivider = !isFirst,
                ) {
                    MyListView__ItemView__ButtonView(
                        text = example.name,
                        rightView = {
                            Row(
                                modifier = Modifier.padding(end = 14.dp)
                            ) {
                                Text(
                                    example.hint,
                                    fontSize = 14.sp,
                                    color = c.text,
                                )
                                AnimatedVisibility(
                                    visible = state.inputUriValue == example.uri,
                                ) {
                                    Icon(
                                        painterResource(id = R.drawable.sf_checkmark_medium_medium),
                                        "Selected",
                                        tint = c.green,
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .size(18.dp)
                                            .alpha(0.8f)
                                            .clip(RoundedCornerShape(99.dp))
                                            .padding(3.dp)
                                    )
                                }
                            }
                        }
                    ) {
                        vm.setInputNameValue(example.name)
                        vm.setInputUriValue(example.uri)
                        keyboardController?.hide()
                    }
                }
            }
        }
    }
}

private val shortcutExamples = listOf(
    ShortcutExample(name = "10-Minute Meditation", hint = "Youtube", uri = "https://www.youtube.com/watch?v=O-6f5wQXSu8"),
    ShortcutExample(name = "Play a Song 😈", hint = "Music App", uri = "https://music.youtube.com/watch?v=ikFFVfObwss&feature=share"),
)

private class ShortcutExample(
    val name: String,
    val hint: String,
    val uri: String,
)
