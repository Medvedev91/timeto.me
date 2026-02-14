package me.timeto.app

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun openGooglePlayAppPage(context: Context) {
    val uri = "https://play.google.com/store/apps/details?id=me.timeto.app".toUri()
    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
}
