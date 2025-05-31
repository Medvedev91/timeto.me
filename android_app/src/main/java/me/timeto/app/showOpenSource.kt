package me.timeto.app

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import me.timeto.shared.openSourceUrl

fun showOpenSource() {
    App.instance.startActivity(
        Intent(Intent.ACTION_VIEW).apply {
            flags = FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse(openSourceUrl)
        }
    )
}
