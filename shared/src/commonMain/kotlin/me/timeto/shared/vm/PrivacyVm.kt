package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.deviceData

class PrivacyVm : __Vm<PrivacyVm.State>() {

    data class State(
        val isSendingReportsEnabled: Boolean,
    ) {

        val title = "Privacy"

        val sendReportsTitle: String =
            "Send Reports${if (isSendingReportsEnabled) "  👍" else "  $prayEmoji"}"

        val textsUi: List<TextUi> = listOf(
            TextUi("Hi,"),
            TextUi("Developer is here."),
            TextUi("The strongest rule I follow is privacy with no compromise."),
            TextUi("The app never sends any personal information!", isBold = true),
            TextUi("All the data the app sends:\n- ${deviceData.os}\n- ${deviceData.device}"),
            TextUi("I kindly ask you 🙏 to turn on sending reports. It is the only way I can know I have such great user like you, nothing else motivates me to keep going."),
        )
    }

    override val state = MutableStateFlow(
        State(
            isSendingReportsEnabled =
                KvDb.KEY.IS_SENDING_REPORTS.selectStringOrNullCached().isSendingReports(),
        )
    )

    init {
        val scopeVm = scopeVm()
        KvDb.KEY.IS_SENDING_REPORTS.selectStringOrNullFlow().onEachExIn(scopeVm) { value ->
            state.update { it.copy(isSendingReportsEnabled = value.isSendingReports()) }
        }
    }

    fun setIsSendingReports(isOn: Boolean) {
        scopeVm().launchEx {
            KvDb.KEY.IS_SENDING_REPORTS.upsertIsSendingReports(isOn)
            if (isOn) launchExIo {
                ping(force = true)
            }
        }
    }

    ///

    class TextUi(
        val text: String,
        val isBold: Boolean = false,
    )
}
