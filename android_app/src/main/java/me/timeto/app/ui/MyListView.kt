package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.timeto.app.*
import me.timeto.app.R

// todo remove bgColor

private val itemMinHeight = 44.dp
private val paddingSectionSection: Dp = itemMinHeight.goldenRatioDown()

//
// Paddings

@Composable
fun MyListView__PaddingFirst() {
    Box(Modifier.height(14.dp))
}

@Composable
fun MyListView__Padding__SectionSection() {
    Box(Modifier.height(paddingSectionSection))
}

@Composable
fun MyListView__Padding__SectionHeader(extraHeight: Dp = 0.dp) {
    Box(Modifier.height(30.dp + extraHeight))
}

@Composable
fun MyListView__Padding__HeaderSection() {
    Box(Modifier.height(4.dp))
}

//
// Header

@Composable
fun MyListView__HeaderView(
    title: String,
    modifier: Modifier = Modifier,
    rightView: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .padding(horizontal = H_PADDING + H_PADDING),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            title,
            modifier = Modifier
                .alpha(0.8f)
                .weight(1f),
            color = c.textSecondary,
            fontSize = 12.sp
        )
        rightView?.invoke()
    }
}

@Composable
fun MyListView__HeaderView__RightIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Icon(
        icon,
        contentDescription,
        tint = c.blue,
        modifier = Modifier
            .padding(start = 10.dp)
            .size(26.dp)
            .offset(y = 4.dp, x = 4.dp)
            .alpha(0.8f)
            .clip(roundedShape)
            .clickable {
                onClick()
            }
            .padding(2.5.dp)
    )
}

//
// Item

@Composable
fun MyListView__ItemView(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    withTopDivider: Boolean = false,
    dividerPadding: PaddingValues = PaddingValues(start = H_PADDING),
    outerPadding: PaddingValues = PaddingValues(horizontal = H_PADDING),
    bgColor: Color = c.sheetFg,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .padding(outerPadding)
            .clip(SquircleShape(12.dp, angles = listOf(isFirst, isFirst, isLast, isLast))),
        contentAlignment = Alignment.TopCenter,
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor),
            contentAlignment = Alignment.CenterStart,
        ) {
            content()
        }

        if (withTopDivider)
            DividerFg(Modifier.padding(dividerPadding), true)
    }
}

@Composable
fun MyListView__ItemView__TextInputView(
    placeholder: String,
    text: String,
    onTextChanged: (String) -> Unit, // WARNING Run only in LaunchedEffect()
    isSingleLine: Boolean = false,
    isAutofocus: Boolean = false,
    keyboardButton: ImeAction = ImeAction.Default,
    keyboardEvent: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }

    Box(
        contentAlignment = Alignment.CenterEnd
    ) {

        val isFocused = remember { mutableStateOf(false) }

        BasicTextField__VMState(
            text = text,
            onValueChange = {
                onTextChanged(it)
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = keyboardButton,
            ),
            keyboardActions = KeyboardActions(
                onAny = { keyboardEvent() },
            ),
            singleLine = isSingleLine,
            cursorBrush = SolidColor(MaterialTheme.colors.primary),
            textStyle = LocalTextStyle.current.copy(
                color = c.text,
                fontSize = 16.sp,
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = itemMinHeight)
                        .padding(
                            start = H_PADDING,
                            end = H_PADDING + 16.dp, // for clear button
                            // top and bottom for multiline padding
                            top = 8.dp + halfDpCeil,
                            bottom = 8.dp,
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (text.isEmpty()) Text(
                        placeholder,
                        style = LocalTextStyle.current.copy(
                            color = c.text.copy(alpha = 0.3f),
                            fontSize = 16.sp
                        )
                    )
                    innerTextField()
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused.value = it.isFocused
                },
        )

        TextFieldClearButtonView(
            text = text,
            isFocused = isFocused.value,
            modifier = Modifier
                .padding(end = 6.dp),
        ) {
            focusRequester.requestFocus()
            onTextChanged("")
        }

        LaunchedEffect(Unit) {
            if (isAutofocus) {
                delay(100) // Otherwise does not work for dialogs
                focusRequester.requestFocus()
            }
        }
    }
}

@Composable
fun MyListView__ItemView__RadioView(
    text: String,
    isActive: Boolean,
    bgColor: Color = c.sheetFg,
    onClick: () -> Unit,
) {
    MyListView__ItemView__ButtonView(
        text = text,
        bgColor = bgColor,
        rightView = {
            Icon(
                if (isActive) Icons.Default.RadioButtonChecked
                else Icons.Default.RadioButtonUnchecked,
                "Toggle",
                tint = c.blue,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(26.dp),
            )
        }
    ) {
        onClick()
    }
}

@Composable
fun MyListView__ItemView__CheckboxView(
    text: String,
    isChecked: Boolean,
    onClick: () -> Unit,
) {
    MyListView__ItemView__ButtonView(
        text = text,
        rightView = {
            AnimatedVisibility(
                visible = isChecked,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Icon(
                    painterResource(id = R.drawable.sf_checkmark_medium_medium),
                    "Checkmark",
                    tint = c.blue,
                    modifier = Modifier
                        .padding(end = H_PADDING)
                        .size(20.dp)
                        .padding(2.dp)
                )
            }
        }
    ) {
        onClick()
    }
}

@Composable
fun MyListView__ItemView__SwitchView(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
) {
    MyListView__Item__Button(
        text = text,
        rightView = {
            Switch(
                checked = isActive,
                onCheckedChange = { onClick() },
                modifier = Modifier
                    .padding(end = 9.dp, bottom = halfDpFloor),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = c.blue,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray,
                ),
            )
        }
    ) {
        onClick()
    }
}

//
// Button

@Composable
fun MyListView__Item__Button(
    text: String,
    maxLines: Int = Int.MAX_VALUE,
    rightView: @Composable () -> Unit,
    onClick: () -> Unit,
) {

    HStack(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .sizeIn(minHeight = itemMinHeight)
            .padding(top = halfDpCeil),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text,
            modifier = Modifier
                .padding(start = H_PADDING, end = 10.dp),
            color = c.text,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )

        SpacerW1()

        rightView()
    }
}

// todo remove
@Composable
fun MyListView__ItemView__ButtonView(
    text: String,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    withArrow: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    bgColor: Color = c.sheetFg,
    rightView: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {

    HStack(
        modifier = modifier
            .background(bgColor) // Fix swipe to action bg on swipe
            .clickable {
                onClick()
            }
            .sizeIn(minHeight = itemMinHeight)
            .padding(top = halfDpCeil),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text,
            modifier = textModifier
                .padding(start = H_PADDING, end = 10.dp),
            color = c.text,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )

        HStack(Modifier.weight(1f)) {
            SpacerW1()
            rightView?.invoke()
        }

        if (withArrow) {
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                "Select emoji",
                tint = c.textSecondary.copy(alpha = 0.4f),
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(28.dp),
            )
        }
    }
}


// todo UI text by center
@Composable
fun MyListView__ItemView__ActionView(
    text: String,
    textColor: Color = c.red,
    onClick: () -> Unit,
) {

    Column(
        modifier = Modifier
            .background(c.sheetFg) // Fix swipe to action bg on swipe
            .clickable {
                onClick()
            },
    ) {

        Row(
            modifier = Modifier
                .sizeIn(minHeight = itemMinHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {

            SpacerW1()

            Text(
                text,
                modifier = Modifier
                    .padding(horizontal = H_PADDING),
                color = textColor,
                fontWeight = FontWeight.W600,
            )

            SpacerW1()
        }
    }
}

@Composable
fun MyListView__Item__Button__RightText(
    text: String,
    color: Color? = null,
    paddingEndExtra: Dp = 0.dp,
    fontSize: TextUnit = TextUnit.Unspecified,
) {

    HStack(
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text,
            modifier = Modifier
                .padding(end = 8.dp + paddingEndExtra)
                .offset(),
            color = color ?: c.tertiaryText,
            fontSize = fontSize,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        MyListView__Item__Button__RightArrow()
    }
}

// todo remove
@Composable
fun MyListView__ItemView__ButtonView__RightText(
    text: String,
    paddingEnd: Dp = H_PADDING,
    color: Color? = null,
) {
    Text(
        text,
        modifier = Modifier
            .padding(end = paddingEnd)
            .offset(),
        color = color ?: c.tertiaryText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun MyListView__Item__Button__RightArrow() {
    Icon(
        painterResource(id = R.drawable.sf_chevron_right_medium_medium),
        "Expand",
        tint = c.tertiaryText,
        modifier = Modifier
            .offset(y = -halfDpFloor)
            .padding(end = H_PADDING - 2.dp - halfDpFloor)
            .size(12.dp),
    )
}
