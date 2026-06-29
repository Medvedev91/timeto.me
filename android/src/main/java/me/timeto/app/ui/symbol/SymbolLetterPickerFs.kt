package me.timeto.app.ui.symbol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.ImeAction
import me.timeto.app.ui.Screen
import me.timeto.app.ui.form.FormInput
import me.timeto.app.ui.form.padding.FormPaddingTop
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.shared.Symbol
import me.timeto.shared.vm.symbol.SymbolLetterPickerUtils

@Composable
fun SymbolLetterPickerFs(
    initText: String,
    onPick: (Symbol.Letter) -> Unit,
) {
    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val formText = remember {
        mutableStateOf(initText)
    }

    Screen {

        Header(
            title = "Symbol",
            scrollState = null,
            actionButton = HeaderActionButton(
                text = "Done",
                isEnabled = true,
                onClick = {
                    SymbolLetterPickerUtils.validateLetter(
                        letter = formText.value,
                        dialogsManager = navigationFs,
                        onSuccess = { symbolLetter ->
                            onPick(symbolLetter)
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

        FormPaddingTop()

        FormInput(
            initText = formText.value,
            placeholder = "Any Symbol",
            onChange = { newName ->
                formText.value = newName
            },
            isFirst = true,
            isLast = true,
            isAutoFocus = true,
            imeAction = ImeAction.Done,
        )
    }
}
