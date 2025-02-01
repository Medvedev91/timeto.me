package me.timeto.shared.ui.privacy

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.launchExIo
import me.timeto.shared.misc.SystemInfo
import me.timeto.shared.onEachExIn
import me.timeto.shared.ping
import me.timeto.shared.prayEmoji
import me.timeto.shared.vm.__Vm

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
            TextUi("All the data the app sends:\n- ${SystemInfo.systemInfo.os.fullVersion}\n- ${SystemInfo.systemInfo.device}"),
            TextUi("I kindly ask you 🙏 to turn on sending reports. It is the only way I can know I have such great user like you, nothing else motivates me to keep going."),
        )
    }

    override val state = MutableStateFlow(
        State(
            isSendingReportsEnabled =
                KvDb.KEY.IS_SENDING_REPORTS.selectOrNullCached().isSendingReports(),
        )
    )

    init {
        val scopeVm = scopeVm()
        KvDb.KEY.IS_SENDING_REPORTS.selectOrNullFlow().onEachExIn(scopeVm) { kvDb ->
            state.update { it.copy(isSendingReportsEnabled = kvDb.isSendingReports()) }
        }
    }

    fun setIsSendingReports(isOn: Boolean) {
        launchExIo {
            KvDb.upsertIsSendingReports(isOn)
            if (isOn)
                ping(force = true)
        }
    }

    ///

    class TextUi(
        val text: String,
        val isBold: Boolean = false,
    )
}
