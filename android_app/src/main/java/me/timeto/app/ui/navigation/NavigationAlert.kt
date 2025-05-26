package me.timeto.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING
import me.timeto.app.c
import me.timeto.app.ui.roundedShape
import me.timeto.app.ui.header.Header__buttonFontSize
import me.timeto.app.ui.header.Header__titleFontWeight

@Composable
fun NavigationAlert(
    message: String,
    withCancelButton: Boolean,
    buttonText: String,
    buttonColor: Color,
    onButtonClick: () -> Unit,
) {

    val navigationLayer = LocalNavigationLayer.current

    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 5.dp),
        color = c.white,
    )

    HStack(
        Modifier
            .fillMaxWidth()
            .padding(top = H_PADDING),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        if (withCancelButton) {
            Text(
                text = "Cancel",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(roundedShape)
                    .clickable {
                        navigationLayer.close()
                    }
                    .padding(
                        horizontal = 12.dp,
                        vertical = 4.dp,
                    ),
                color = c.textSecondary,
                fontSize = Header__buttonFontSize,
            )
        }

        Text(
            text = buttonText,
            modifier = Modifier
                .clip(roundedShape)
                .background(buttonColor)
                .clickable {
                    onButtonClick()
                }
                .padding(
                    horizontal = 12.dp,
                    vertical = 4.dp,
                ),
            color = c.text,
            fontSize = Header__buttonFontSize,
            fontWeight = Header__titleFontWeight,
        )
    }
}
