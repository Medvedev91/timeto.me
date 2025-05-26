package me.timeto.app.misc

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import me.timeto.app.App
import me.timeto.shared.OPEN_SOURCE_URL

fun showOpenSource() {
    App.instance.startActivity(
        Intent(Intent.ACTION_VIEW).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(OPEN_SOURCE_URL)
        }
    )
}
