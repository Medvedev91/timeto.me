package me.timeto.app.ui.footer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.H_PADDING_HALF
import me.timeto.app.R
import me.timeto.app.c
import me.timeto.app.ui.squircleShape

@Composable
fun FooterAddButton(
    text: String,
    onClick: () -> Unit,
) {

    HStack(
        modifier = Modifier
            .clip(squircleShape)
            .clickable {
                onClick()
            }
            .padding(horizontal = H_PADDING_HALF, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            painter = painterResource(R.drawable.sf_plus_circle_fill_medium_bold),
            contentDescription = text,
            tint = c.blue,
            modifier = Modifier
                .size(20.dp),
        )

        Text(
            text = text,
            modifier = Modifier
                .padding(start = 8.dp),
            color = c.blue,
            fontWeight = FontWeight.Bold,
        )
    }
}
