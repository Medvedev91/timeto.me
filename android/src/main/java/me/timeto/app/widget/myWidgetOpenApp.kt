package me.timeto.app.widget

import android.content.Context

fun myWidgetOpenApp(context: Context) {
    context.startActivity(
        context.packageManager.getLaunchIntentForPackage(context.packageName)
    )
}
