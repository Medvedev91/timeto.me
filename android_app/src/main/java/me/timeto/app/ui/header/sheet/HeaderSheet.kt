package me.timeto.app.ui.header.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.timeto.app.H_PADDING_HALF
import me.timeto.app.ui.ZStack
import me.timeto.app.c
import me.timeto.app.squircleShape

@Composable
fun HeaderSheet(
    title: String,
    doneButton: HeaderSheetButton,
    cancelButton: HeaderSheetButton,
) {

    ZStack(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(horizontal = H_PADDING_HALF),
    ) {

        Text(
            text = cancelButton.text,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(squircleShape)
                .clickable {
                    cancelButton.onClick()
                }
                .padding(
                    horizontal = H_PADDING_HALF,
                    vertical = 4.dp,
                ),
            color = c.blue,
        )

        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.Center),
            color = c.white,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = doneButton.text,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clip(squircleShape)
                .clickable {
                    doneButton.onClick()
                }
                .padding(
                    horizontal = H_PADDING_HALF,
                    vertical = 4.dp,
                ),
            color = c.blue,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
