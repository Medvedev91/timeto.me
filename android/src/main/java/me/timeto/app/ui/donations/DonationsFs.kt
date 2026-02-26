package me.timeto.app.ui.donations

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.ui.Screen
import me.timeto.app.ui.VStack
import me.timeto.app.ui.ZStack
import me.timeto.app.ui.c
import me.timeto.app.ui.header.Header
import me.timeto.app.ui.header.HeaderActionButton
import me.timeto.app.ui.header.HeaderCancelButton
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.navigation.LocalNavigationLayer
import me.timeto.app.ui.rememberVm
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.donations.DonationsVm

@Composable
fun DonationsFs() {

    val navigationFs = LocalNavigationFs.current
    val navigationLayer = LocalNavigationLayer.current

    val (vm, state) = rememberVm {
        DonationsVm()
    }

    val isDonated = state.supporterEmail != null

    BackHandler {}

    val scrollState = rememberScrollState()

    Screen {

        Header(
            title = "Donations",
            scrollState = scrollState,
            actionButton =
                if (isDonated) HeaderActionButton(
                    text = "Done",
                    isEnabled = true,
                    onClick = {
                        navigationLayer.close()
                    },
                )
                else null,
            cancelButton =
                if (!isDonated) HeaderCancelButton(
                    text = "Another Time",
                    onClick = {
                        vm.onTapAnotherTime()
                        navigationLayer.close()
                    },
                )
                else null,
        )

        VStack(
            modifier = Modifier
                .verticalScroll(state = scrollState),
        ) {

            TextView(
                text = buildAnnotatedString {
                    append("timeto.me is 100% free and open source, I only ask for donations.")
                },
            )

            TextView(
                text = buildAnnotatedString {
                    append("Please donate any amount here ")
                    val bmcUrl = "https://buymeacoffee.com/medvedev91"
                    withLink(
                        LinkAnnotation.Url(
                            url = bmcUrl,
                            styles = TextLinkStyles(style = SpanStyle(color = c.blue)),
                        )
                    ) {
                        append(bmcUrl)
                    }
                    append(" and enter the supporter's email to hide donation notifications.")
                }
            )

            TextView(
                text = buildAnnotatedString {
                    append("I would be especially grateful for \"Make this monthly\" enabled. But! Only if I deserve it! 😉")
                },
            )

            if (isDonated) {
                TextView(
                    text = buildAnnotatedString {
                        append(state.activatedMessage ?: "Thank you!")
                    },
                    color = c.green,
                )
            } else {
                HStack(
                    modifier = Modifier
                        .padding(
                            start = H_PADDING,
                            end = H_PADDING,
                            top = 22.dp,
                        ),
                ) {

                    val textField = remember {
                        val stateText: String = state.supporterEmail ?: ""
                        mutableStateOf(TextFieldValue(stateText, TextRange(stateText.length)))
                    }

                    val inputText: String = textField.value.text
                    val inputHeight = 40.dp
                    BasicTextField(
                        value = textField.value,
                        onValueChange = { newValue ->
                            textField.value = newValue
                        },
                        modifier = Modifier
                            .weight(1f),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Email,
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
                                    .sizeIn(minHeight = inputHeight)
                                    .clip(roundedShape)
                                    .background(c.gray6)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "Supporter's Email",
                                        style = LocalTextStyle.current.copy(
                                            color = c.text.copy(alpha = 0.3f),
                                            fontSize = 16.sp,
                                        ),
                                    )
                                }
                                innerTextField()
                            }
                        },
                    )

                    val isButtonEnabled: Boolean =
                        !state.isActivationInProgress && inputText.isNotBlank()
                    ZStack(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clip(roundedShape)
                            .height(inputHeight)
                            .alpha(if (isButtonEnabled) 1f else .5f)
                            .background(c.blue)
                            .clickable(isButtonEnabled) {
                                vm.activate(
                                    email = inputText,
                                    dialogsManager = navigationFs,
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Save",
                            modifier = Modifier
                                .padding(horizontal = 12.dp),
                            color = c.white,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            VStack(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding()
                    .imePadding(),
            ) {}
        }
    }
}

@Composable
private fun TextView(
    text: AnnotatedString,
    color: Color = c.text,
) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = H_PADDING)
            .padding(top = 16.dp),
        color = color,
        lineHeight = 22.sp,
    )
}
