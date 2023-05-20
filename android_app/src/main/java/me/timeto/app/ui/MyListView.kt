package me.timeto.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.R
import me.timeto.app.toColor
import kotlinx.coroutines.delay
import me.timeto.shared.ColorNative

object MyListView {

    val ITEM_MIN_HEIGHT = 46.dp

    val PADDING_OUTER_HORIZONTAL = 20.dp
    val PADDING_INNER_HORIZONTAL = 16.dp
}

///
/// Paddings

@Composable
fun MyListView__Padding__SectionSection() {
    Box(Modifier.height(34.dp))
}

@Composable
fun MyListView__Padding__SectionHeader(extraHeight: Dp = 0.dp) {
    Box(Modifier.height(30.dp + extraHeight))
}

@Composable
fun MyListView__Padding__HeaderSection() {
    Box(Modifier.height(4.dp))
}

///
/// Header

@Composable
fun MyListView__HeaderView(
    title: String,
    modifier: Modifier = Modifier,
    rightView: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .padding(horizontal = MyListView.PADDING_OUTER_HORIZONTAL + MyListView.PADDING_INNER_HORIZONTAL),
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
            .clip(RoundedCornerShape(99.dp))
            .clickable {
                onClick()
            }
            .padding(2.5.dp)
    )
}

///
/// Item

@Composable
fun MyListView__ItemView(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    withTopDivider: Boolean = false,
    dividerPadding: PaddingValues = PaddingValues(start = MyListView.PADDING_INNER_HORIZONTAL),
    outerPadding: PaddingValues = PaddingValues(horizontal = MyListView.PADDING_OUTER_HORIZONTAL),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .padding(outerPadding)
            .clip(MySquircleShape(angles = listOf(isFirst, isFirst, isLast, isLast))),
        contentAlignment = Alignment.TopCenter,
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.background2),
            contentAlignment = Alignment.CenterStart,
        ) {
            content()
        }

        if (withTopDivider)
            Divider(
                color = c.dividerBg2,
                modifier = Modifier
                    .padding(dividerPadding),
                thickness = 0.5.dp,
            )
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
                color = MaterialTheme.colors.onSurface,
                fontSize = 16.sp
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(minHeight = MyListView.ITEM_MIN_HEIGHT)
                        .padding(
                            start = MyListView.PADDING_INNER_HORIZONTAL,
                            end = MyListView.PADDING_INNER_HORIZONTAL + 12.dp, // for clear button
                            // top and bottom for multiline padding
                            top = 8.dp,
                            bottom = 8.dp,
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (text.isEmpty()) Text(
                        placeholder,
                        style = LocalTextStyle.current.copy(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                            fontSize = 16.sp
                        )
                    )
                    innerTextField()
                }
            },
            modifier = Modifier
                .focusRequester(focusRequester)
        )

        TextFieldClearButtonView(text = text) {
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
    onClick: () -> Unit,
) {
    MyListView__ItemView__ButtonView(
        text = text,
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
                        .padding(end = MyListView.PADDING_INNER_HORIZONTAL)
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
    MyListView__ItemView__ButtonView(
        text = text,
        rightView = {
            Switch(
                checked = isActive,
                onCheckedChange = { onClick() },
                modifier = Modifier.padding(end = 6.dp),
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

///
/// Button

@Composable
fun MyListView__ItemView__ButtonView(
    text: String,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    withArrow: Boolean = false,
    rightView: @Composable (() -> Unit)? = null,
    bottomView: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {

    Column(
        modifier = modifier
            .background(c.background2) // Fix swipe to action bg on swipe
            .clickable {
                onClick()
            },
    ) {

        Row(
            modifier = Modifier
                .sizeIn(minHeight = MyListView.ITEM_MIN_HEIGHT),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text,
                modifier = textModifier
                    .padding(start = MyListView.PADDING_INNER_HORIZONTAL, end = 10.dp),
                color = c.text,
            )

            Row(Modifier.weight(1f)) {
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
                        .size(28.dp)
                )
            }
        }

        bottomView?.invoke()
    }
}


@Composable
fun MyListView__ItemView__ActionView(
    text: String,
    textColor: ColorNative = ColorNative.red,
    onClick: () -> Unit,
) {

    Column(
        modifier = Modifier
            .background(c.background2) // Fix swipe to action bg on swipe
            .clickable {
                onClick()
            },
    ) {

        Row(
            modifier = Modifier
                .sizeIn(minHeight = MyListView.ITEM_MIN_HEIGHT),
            verticalAlignment = Alignment.CenterVertically
        ) {

            SpacerW1()

            Text(
                text,
                modifier = Modifier
                    .padding(horizontal = MyListView.PADDING_INNER_HORIZONTAL),
                color = textColor.toColor(),
                fontWeight = FontWeight.W600,
            )

            SpacerW1()
        }
    }
}

@Composable
fun MyListView__ItemView__ButtonView__RightText(
    text: String,
    paddingEnd: Dp = MyListView.PADDING_INNER_HORIZONTAL,
    color: Color? = null,
) {
    Text(
        text,
        modifier = Modifier
            .padding(end = paddingEnd)
            .offset(),
        color = color ?: c.formButtonRightNoteText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
