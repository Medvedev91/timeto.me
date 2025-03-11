package me.timeto.app.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.timeto.app.H_PADDING
import me.timeto.app.ZStack
import me.timeto.app.c
import me.timeto.app.halfDpCeil
import me.timeto.app.ui.form.views.FormItemView

@Composable
fun FormInput(
    initText: String,
    placeholder: String,
    onChange: (String) -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    isAutoFocus: Boolean,
    imeAction: ImeAction,
    triggerReinit: Any = Unit,
) {

    val textField = remember(triggerReinit) {
        mutableStateOf(TextFieldValue(initText, TextRange(initText.length)))
    }

    val text: String = textField.value.text

    val focusRequester = remember { FocusRequester() }
    val isFocused = remember { mutableStateOf(false) }

    FormItemView(
        isFirst = isFirst,
        isLast = isLast,
        modifier = Modifier,
        content = {
            BasicTextField(
                value = textField.value,
                onValueChange = { newValue ->
                    textField.value = newValue
                    onChange(newValue.text)
                },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        isFocused.value = it.isFocused
                    },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = imeAction,
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
                            .sizeIn(minHeight = Form__itemMinHeight)
                            .padding(
                                start = H_PADDING,
                                end = H_PADDING + 16.dp, // For clear button
                                // Top and bottom for multiline padding
                                top = 8.dp + halfDpCeil,
                                bottom = 8.dp,
                            ),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = LocalTextStyle.current.copy(
                                    color = c.text.copy(alpha = 0.3f),
                                    fontSize = 16.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                },
            )
        },
    )

    LaunchedEffect(Unit) {
        if (isAutoFocus) {
            delay(100) // Otherwise does not work for dialogs
            focusRequester.requestFocus()
        }
    }
}
