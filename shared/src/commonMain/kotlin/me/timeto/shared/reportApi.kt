package me.timeto.shared

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.coroutines.launch
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.misc.SystemInfo
import me.timeto.shared.misc.ioScope
import me.timeto.shared.misc.zlog

fun reportApi(
    message: String,
) {

    // Not launchEx because of recursion
    ioScope().launch {

        if (!KvDb.KEY.IS_SENDING_REPORTS.selectOrNull().isSendingReports())
            return@launch

        val title: String = when (SystemInfo.instance.os) {
            is SystemInfo.Os.Android -> "🤖 Android"
            is SystemInfo.Os.Ios -> " iOS"
            is SystemInfo.Os.Watchos -> "⌚ Watch OS"
        }

        zlog("reportApi $message")
        try {
            HttpClient().use {
                val token = KvDb.selectTokenOrNullSafe()
                it.submitForm(
                    url = "https://api.timeto.me/report",
                    formParameters = Parameters.build {
                        append("title", title)
                        append("message", message)
                    }
                ) {
                    appendSystemInfo(token)
                }
            }
        } catch (e: Throwable) {
            // todo report by fallback way
            // Cases:
            // - no internet connection
            // - todo check if domain unavailable
            // - todo check if not "ok" returned
            zlog("reportApi exception:\n$e")
        }
    }
}
