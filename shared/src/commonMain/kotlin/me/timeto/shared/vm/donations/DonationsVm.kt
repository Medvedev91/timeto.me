package me.timeto.shared.vm.donations

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import me.timeto.shared.DialogsManager
import me.timeto.shared.db.KvDb
import me.timeto.shared.getString
import me.timeto.shared.ioScope
import me.timeto.shared.launchEx
import me.timeto.shared.reportApi
import me.timeto.shared.time
import me.timeto.shared.vm.Vm

class DonationsVm : Vm<DonationsVm.State>() {

    data class State(
        val supporterEmail: String?,
        val activatedMessage: String?,
        val isActivationInProgress: Boolean,
    )

    override val state = MutableStateFlow<State>(
        State(
            supporterEmail = KvDb.KEY.DONATIONS_EMAIL.selectStringOrNullCached(),
            activatedMessage = KvDb.KEY.DONATIONS_ACTIVATED_MESSAGE.selectStringOrNullCached(),
            isActivationInProgress = false,
        )
    )

    init {
        val scopeVm = scopeVm()
        combine(
            KvDb.KEY.DONATIONS_EMAIL.selectStringOrNullFlow(),
            KvDb.KEY.DONATIONS_ACTIVATED_MESSAGE.selectStringOrNullFlow(),
        ) { supporterEmail, activatedMessage ->
            state.update {
                it.copy(
                    supporterEmail = supporterEmail,
                    activatedMessage = activatedMessage,
                )
            }
        }.launchIn(scopeVm)
    }

    fun onTapAnotherTime() {
        ioScope().launchEx {
            KvDb.KEY.DONATIONS_TIME.upsertInt(-time())
        }
    }

    fun activate(
        email: String,
        dialogsManager: DialogsManager,
    ) {
        state.update { it.copy(isActivationInProgress = true) }
        ioScope().launchEx {
            try {
                HttpClient().use { client ->
                    val apiUrl = "https://api.timeto.me/donation?email=${email.trim()}"
                    val httpResponse = client.get(apiUrl)
                    val plainJson = httpResponse.bodyAsText()
                    val j = Json.parseToJsonElement(plainJson).jsonObject
                    val status: String = j.getString("status")
                    if (status == "error") {
                        dialogsManager.alert(j.getString("message"))
                        return@launchEx
                    }
                    val jData = j.jsonObject["data"]!!.jsonObject
                    val message: String = jData.getString("message")
                    val newEmail: String = jData.getString("email")
                    KvDb.KEY.DONATIONS_ACTIVATED_MESSAGE.upsertString(message)
                    KvDb.KEY.DONATIONS_EMAIL.upsertString(newEmail)
                    KvDb.KEY.DONATIONS_TIME.upsertInt(time())
                    dialogsManager.alert(message)
                }
            } catch (e: Throwable) {
                reportApi("DonationsVm() activate error: $e")
                dialogsManager.alert("Error. Please contact us.")
            } finally {
                // "finally" to handle like "return@launchEx"
                state.update { it.copy(isActivationInProgress = false) }
            }
        }
    }
}
