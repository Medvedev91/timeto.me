package me.timeto.shared

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.isSendingReports

var pingLastDay: Int? = null

suspend fun ping(
    force: Boolean = false,
) {
    try {

        val today = UnixTime().localDay

        if (!force) {
            if (!KvDb.KEY.IS_SENDING_REPORTS.selectOrNull().isSendingReports())
                return
            if (pingLastDay == today)
                return
        }

        HttpClient().use { client ->
            val httpResponse = client.get("https://api.timeto.me/ping") {
                val token = KvDb.selectTokenOrNullSafe()
                val password = getsertTokenPassword()
                url {
                    parameters.append("password", password)
                    urlAppendSystemInfo(token)
                }
            }
            val plainJson = httpResponse.bodyAsText()
            val j = Json.parseToJsonElement(plainJson).jsonObject
            if (j.getString("status") != "success")
                throw Exception("status != success\n$plainJson")
            val jData = j.jsonObject["data"]!!.jsonObject
            KvDb.KEY.TOKEN.upsertString(jData.getString("token"))
            KvDb.KEY.FEEDBACK_SUBJECT.upsertString(jData.getString("feedback_subject"))
            pingLastDay = today // After success
        }
    } catch (e: Throwable) {
        reportApi("ping() exception:\n$e")
    }
}

private suspend fun getsertTokenPassword(): String {
    val oldPassword = KvDb.KEY.TOKEN_PASSWORD.selectStringOrNull()
    if (oldPassword != null)
        return oldPassword
    val chars = ('0'..'9') + ('a'..'z') + ('A'..'Z') + ("!@#%^&*()_+".toList())
    val newPassword = (1..15).map { chars.random() }.joinToString("")
    KvDb.KEY.TOKEN_PASSWORD.upsertString(newPassword)
    return newPassword
}
