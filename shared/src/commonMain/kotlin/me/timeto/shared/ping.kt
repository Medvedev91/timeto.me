package me.timeto.shared

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.timeto.shared.db.KvDb

val pingTriggerFlow = MutableStateFlow(0)

suspend fun ping(
    notificationsPermission: NotificationsPermission,
) {
    try {
        HttpClient().use { client ->
            val httpResponse = client.get("https://api.timeto.me/ping") {
                val token = KvDb.selectTokenOrNullSafe()
                val password = getsertTokenPassword()
                url {
                    parameters.append("password", password)
                    parameters.append("notifications_permission", notificationsPermission.toUrlParam())
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
        }
    } catch (e: Throwable) {
        reportApi("ping() exception:\n$e")
        ioScope().launch {
            try {
                delay(10 * 60 * 1_000L) // 10 min
                pingTriggerFlow.emit(pingTriggerFlow.value + 1)
            } catch (_: CancellationException) {
                // On app close
            }
        }
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

private fun NotificationsPermission.toUrlParam(): String = when (this) {
    NotificationsPermission.notAsked -> "not_asked"
    NotificationsPermission.denied -> "denied"
    NotificationsPermission.rationale -> "rationale"
    NotificationsPermission.granted -> "granted"
}
