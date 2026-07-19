package me.timeto.app.ui.note_folder

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.c
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.FormSwitch
import me.timeto.app.ui.form.button.FormButtonSymbol
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.symbol.SymbolPickerFs
import me.timeto.shared.Symbol
import me.timeto.shared.db.NoteFolderDb
import me.timeto.shared.vm.note_folder_form.NoteFolderFormVm

@Composable
fun NoteFolderFormFs(
    noteFolderDb: NoteFolderDb?,
    onDelete: () -> Unit,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        NoteFolderFormVm(
            noteFolderDb = noteFolderDb,
        )
    }

    Screen {

        val scrollState = rememberLazyListState()

        Header(
            title = state.title,
            scrollState = scrollState,
            actionButton = HeaderActionButton(
                text = state.doneText,
                isEnabled = state.isSaveEnabled,
                onClick = {
                    vm.save(
                        dialogsManager = navigationFs,
                        onSuccess = {
                            navigationLayer.close()
                        },
                    )
                },
            ),
            cancelButton = HeaderCancelButton(
                text = "Cancel",
                onClick = {
                    navigationLayer.close()
                },
            ),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(bottom = 25.dp),
        ) {

            item {
                FormPaddingTop()
            }

            item {
                FormInput(
                    initText = state.name,
                    placeholder = state.namePlaceholder,
                    onChange = { newName ->
                        vm.setName(newName)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = true,
                    imeAction = ImeAction.Done,
                )
            }

            item {

                FormPaddingSectionSection()

                fun showSymbolPicker() {
                    navigationFs.push {
                        SymbolPickerFs(
                            onPick = { symbol ->
                                vm.setSymbol(symbol)
                            },
                        )
                    }
                }

                val symbol: Symbol? = state.symbol
                if (symbol == null) {
                    FormButton(
                        title = state.iconTitle,
                        isFirst = true,
                        isLast = false,
                        note = "Not Selected",
                        noteColor = c.red,
                        withArrow = true,
                        onClick = {
                            showSymbolPicker()
                        },
                    )
                } else {
                    FormButtonSymbol(
                        title = state.iconTitle,
                        symbol = symbol,
                        color = c.secondaryText,
                        withArrow = true,
                        isFirst = true,
                        isLast = false,
                        onClick = {
                            showSymbolPicker()
                        },
                    )
                }

                FormSwitch(
                    title = state.onHomeTitle,
                    isEnabled = state.onHome,
                    isFirst = false,
                    isLast = true,
                    modifier = Modifier,
                    onChange = { newValue ->
                        vm.setOnHome(newValue)
                    },
                )
            }

            if (noteFolderDb != null) {
                item {
                    FormPaddingSectionSection()
                    FormButton(
                        title = state.deleteText,
                        titleColor = c.red,
                        isFirst = true,
                        isLast = true,
                        onClick = {
                            vm.delete(
                                noteFolderDb = noteFolderDb,
                                dialogsManager = navigationFs,
                                onDelete = {
                                    navigationLayer.close()
                                    onDelete()
                                },
                            )
                        },
                    )
                }
            }
        }
    }
}
