package me.timeto.app.ui.activities.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.HStack
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.roundedShape
import me.timeto.app.toColor
import me.timeto.app.ui.Screen
import me.timeto.app.ui.color.ColorPickerFs
import me.timeto.app.ui.emoji.EmojiPickerFs
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormPaddingSectionSection
import me.timeto.app.ui.form.FormPaddingTop
import me.timeto.app.ui.form.views.FormButtonArrowView
import me.timeto.app.ui.form.views.FormButtonNoteView
import me.timeto.app.ui.form.views.FormButtonView
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.ActivityDb
import me.timeto.shared.ui.activities.form.ActivityFormVm

@Composable
fun ActivityFormFs(
    initActivityDb: ActivityDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        ActivityFormVm(
            initActivityDb = initActivityDb,
        )
    }

    Screen(
        modifier = Modifier
            .imePadding(),
    ) {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.saveText,
                isEnabled = true,
                onClick = {
                    vm.save(
                        dialogsManager = navigationFs,
                        onSave = {
                            navigationLayer.close()
                        }
                    )
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(bottom = 25.dp),
        ) {

            item {

                FormPaddingTop()

                FormInput(
                    initText = state.name,
                    placeholder = state.namePlaceholder,
                    onChange = { newName ->
                        vm.setName(newName)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = false,
                    imeAction = ImeAction.Done,
                )

                FormPaddingSectionSection()

                FormButtonView(
                    title = state.emojiTitle,
                    titleColor = null,
                    isFirst = true,
                    isLast = false,
                    modifier = Modifier,
                    rightView = {
                        HStack(
                            verticalAlignment = CenterVertically,
                        ) {
                            val emoji: String? = state.emoji
                            if (emoji != null) {
                                Text(
                                    text = emoji,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .padding(end = 9.dp)
                                )
                            } else {
                                FormButtonNoteView(
                                    note = state.emojiNotSelected,
                                    color = c.red,
                                    withArrow = true,
                                )
                            }
                            FormButtonArrowView()
                        }
                    },
                    onClick = {
                        navigationFs.push {
                            EmojiPickerFs(
                                onPick = { newEmoji ->
                                    vm.setEmoji(newEmoji = newEmoji)
                                },
                            )
                        }
                    },
                    onLongClick = null,
                )

                FormButtonView(
                    title = state.colorTitle,
                    titleColor = null,
                    isFirst = false,
                    isLast = true,
                    modifier = Modifier,
                    rightView = {
                        HStack(
                            verticalAlignment = CenterVertically,
                        ) {
                            ZStack(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(28.dp)
                                    .clip(roundedShape)
                                    .background(state.colorRgba.toColor()),
                            )
                            FormButtonArrowView()
                        }
                    },
                    onClick = {
                        navigationFs.push {
                            ColorPickerFs(
                                title = state.colorTitle,
                                initExamplesData = state.buildColorPickerExamplesData(),
                                onPick = { newColorRgba ->
                                    vm.setColorRgba(newColorRgba = newColorRgba)
                                },
                            )
                        }
                    },
                    onLongClick = null,
                )
            }
        }
    }
}
