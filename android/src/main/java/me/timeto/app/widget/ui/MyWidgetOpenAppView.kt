package me.timeto.app.widget.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.timeto.app.ui.c

@Composable
fun MyWidgetOpenAppView() {
    val context: Context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(c.black)
            .clickable {
                context.startActivity(
                    context.packageManager.getLaunchIntentForPackage(context.packageName)
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Please open the app.",
            style = TextStyle(
                color = ColorProvider(day = Color.White, night = Color.White),
            ),
        )
    }
}
