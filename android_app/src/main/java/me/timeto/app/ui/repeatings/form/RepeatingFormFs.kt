package me.timeto.app.ui.repeatings.form

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import me.timeto.app.c
import me.timeto.app.rememberVm
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.button.FormButton
import me.timeto.app.ui.form.padding.FormPaddingSectionSection
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.db.RepeatingDb
import me.timeto.shared.ui.repeatings.form.RepeatingFormVm

@Composable
fun RepeatingFormFs(
    initRepeatingDb: RepeatingDb?,
) {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        RepeatingFormVm(
            initRepeatingDb = initRepeatingDb,
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
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState,
        ) {

            item {

                FormPaddingTop()

                FormInput(
                    initText = state.text,
                    placeholder = state.textPlaceholder,
                    onChange = { newText ->
                        vm.setText(newText)
                    },
                    isFirst = true,
                    isLast = true,
                    isAutoFocus = false,
                    imeAction = ImeAction.Done,
                )

                FormPaddingSectionSection()

                FormButton(
                    title = state.periodTitle,
                    isFirst = true,
                    isLast = false,
                    note = state.periodNote,
                    noteColor = if (state.period == null) c.red else c.textSecondary,
                    withArrow = true,
                    onClick = {
                        navigationFs.push {
                            RepeatingFormPeriodFs(
                                initPeriod = state.period,
                                onDone = { newPeriod ->
                                    vm.setPeriod(newPeriod)
                                },
                            )
                        }
                    },
                )
            }
        }
    }
}
