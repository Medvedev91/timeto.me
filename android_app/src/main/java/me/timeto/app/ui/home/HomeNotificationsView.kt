package me.timeto.app.ui.home

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.timeto.app.MainActivity
import me.timeto.app.R
import me.timeto.app.openNotificationSettings
import me.timeto.app.ui.*
import me.timeto.shared.NotificationsPermission
import me.timeto.shared.reportApi

@Composable
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun HomeNotificationsView(
    title: String,
    buttonText: String,
    notificationsPermission: NotificationsPermission,
) {
    val mainActivity = LocalActivity.current as MainActivity

    HStack(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, bottom = 20.dp),
    ) {

        Icon(
            painter = painterResource(id = R.drawable.sf_bell_slash_medium),
            contentDescription = "Notifications",
            tint = c.white,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(48.dp),
        )

        VStack(
            modifier = Modifier
                .padding(start = 16.dp),
            horizontalAlignment = Alignment.Start,
        ) {

            Text(
                text = title,
                modifier = Modifier
                    .padding(start = onePx),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Medium,
                color = c.white,
                lineHeight = 20.sp,
            )

            Text(
                text = buttonText,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(HomeScreen__itemCircleHeight + 2.dp)
                    .clip(roundedShape)
                    .background(c.white)
                    .clickable {
                        when (notificationsPermission) {
                            NotificationsPermission.notAsked -> {
                                reportApi("HomeNotificationsView.kt: Impossible Not Asked")
                                openNotificationSettings(mainActivity)
                            }

                            NotificationsPermission.denied -> {
                                openNotificationSettings(mainActivity)
                            }

                            NotificationsPermission.rationale -> {
                                mainActivity.requestNotificationsPermission()
                            }

                            NotificationsPermission.granted -> {
                                reportApi("HomeNotificationsView.kt: Impossible Granted")
                                openNotificationSettings(mainActivity)
                            }
                        }
                    }
                    .padding(horizontal = 10.dp),
                fontSize = HomeScreen__itemCircleFontSize,
                fontWeight = HomeScreen__itemCircleFontWeight,
            )
        }
    }
}
