package me.timeto.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.VStack
import me.timeto.app.ui.c
import me.timeto.app.ui.navigation.LocalNavigationFs
import me.timeto.app.ui.readme.ReadmeFs
import me.timeto.app.ui.roundedShape

private val shape = SquircleShape(12.dp)
private val bottomMargin: Dp =
    (HomeScreen__itemHeight - HomeScreen__itemCircleHeight) / 2

@Composable
fun HomeReadmeView(
    title: String,
    buttonText: String,
    onButtonClick: () -> Unit,
) {

    val navigationFs = LocalNavigationFs.current

    VStack(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HomeScreen__hPadding)
            .padding(bottom = bottomMargin)
            .clip(shape)
            .background(c.blue)
            .padding(top = 10.dp, bottom = bottomMargin * 2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = title,
            modifier = Modifier
                .padding(horizontal = HomeScreen__hPadding),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = c.white,
        )

        Text(
            text = buttonText,
            modifier = Modifier
                .padding(top = 7.dp)
                .height(HomeScreen__itemCircleHeight + 2.dp)
                .clip(roundedShape)
                .background(c.white)
                .clickable {
                    onButtonClick()
                    navigationFs.push {
                        ReadmeFs()
                    }
                }
                .padding(horizontal = 10.dp),
            fontSize = HomeScreen__itemCircleFontSize,
            fontWeight = HomeScreen__itemCircleFontWeight,
        )
    }
}
