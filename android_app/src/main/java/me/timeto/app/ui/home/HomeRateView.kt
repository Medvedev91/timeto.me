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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.ui.HStack
import me.timeto.app.ui.SquircleShape
import me.timeto.app.ui.VStack
import me.timeto.app.ui.c
import me.timeto.app.ui.roundedShape
import me.timeto.shared.vm.home.HomeVm
import me.timeto.app.openGooglePlayAppPage

private val shape = SquircleShape(12.dp)
private val bottomMargin: Dp =
    (HomeScreen__itemHeight - HomeScreen__itemCircleHeight) / 2

@Composable
fun HomeRateView(
    homeVm: HomeVm,
    homeState: HomeVm.State,
) {

    VStack(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HomeScreen__hPadding)
            .padding(top = 4.dp, bottom = bottomMargin)
            .clip(shape)
            .background(c.blue)
            .padding(top = 10.dp, bottom = bottomMargin * 1.62f),
        horizontalAlignment = Alignment.Start,
    ) {

        Text(
            text = homeState.rateLine1,
            modifier = Modifier
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Medium,
            color = c.white,
        )

        Text(
            text = homeState.rateLine2,
            modifier = Modifier
                .padding(top = 2.dp)
                .padding(horizontal = 12.dp),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            color = c.white,
        )

        HStack(
            modifier = Modifier
                .padding(top = 8.dp, start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            val context = LocalContext.current

            HStack(
                modifier = Modifier
                    .height(HomeScreen__itemCircleHeight + 2.dp)
                    .clip(roundedShape)
                    .background(c.white)
                    .clickable {
                        homeVm.onRateStart()
                        openGooglePlayAppPage(context = context)
                    }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Write a Review on Google Play",
                    fontSize = HomeScreen__itemCircleFontSize,
                    fontWeight = HomeScreen__itemCircleFontWeight,
                )
                Text(
                    text = "🙏",
                    modifier = Modifier
                        .padding(start = 5.dp),
                    fontSize = HomeScreen__itemCircleFontSize,
                    fontWeight = HomeScreen__itemCircleFontWeight,
                )
            }

            Text(
                text = homeState.rateNoThanks,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(roundedShape)
                    .clickable {
                        homeVm.onRateCancel()
                    }
                    .padding(horizontal = 2.dp),
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = FontWeight.Medium,
                color = c.white,
            )
        }
    }
}
