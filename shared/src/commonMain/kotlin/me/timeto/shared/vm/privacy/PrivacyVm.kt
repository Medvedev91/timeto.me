package me.timeto.shared.vm.privacy

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.launchExIo
import me.timeto.shared.SystemInfo
import me.timeto.shared.onEachExIn
import me.timeto.shared.prayEmoji
import me.timeto.shared.vm.Vm

class PrivacyVm : Vm<PrivacyVm.State>() {

    data class State(
        val isSendingReportsEnabled: Boolean,
    ) {

        val title = "Privacy"

        val sendReportsTitle: String =
            "Send Diagnostic Data${if (isSendingReportsEnabled) "  ❤️" else "  $prayEmoji"}"

        val textsUi: List<TextUi> = listOf(
            TextUi("The app never sends any personal information!", isBold = true),
            TextUi("The only data the app sends:\n- ${SystemInfo.instance.os.fullVersion}\n- ${SystemInfo.instance.device}"),
            TextUi("I kindly ask you 🙏 to turn on sending diagnostics. It is the only way I can know I have such great user like you. Nothing else motivates me to keep going."),
            TextUi("You can change it anytime."),
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

    fun setIsSendingReports(isEnabled: Boolean) {
        launchExIo {
            KvDb.upsertIsSendingReports(isEnabled)
        }
    }

    ///

    class TextUi(
        val text: String,
        val isBold: Boolean = false,
    )
}
