package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.*
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.deviceData

class PrivacySheetVm : __Vm<PrivacySheetVm.State>() {

    data class State(
        val isSendReportsEnabled: Boolean,
    ) {

        val headerTitle = "Privacy"

        val sendReportsTitle = "Send Reports${if (isSendReportsEnabled) "" else "  $prayEmoji"}"

        val text1 = textHi
        val text2 = textHere
        val text3 = textRule
        val text4 = textNever
        val text5 = textOnly
        val text6 = textKindly

        val sendItems: List<String> = PrivacySheetVm.sendItems
    }

    override val state = MutableStateFlow(
        State(
            isSendReportsEnabled = false, // todo init value
        )
    )

    override fun onAppear() {
        val scope = scopeVm()
        KvDb.KEY.IS_SENDING_REPORTS.getOrNullFlow().onEachExIn(scope) { kvDb ->
            state.update {
                val newVal = kvDb?.value.isSendingReports()
                it.copy(isSendReportsEnabled = newVal)
            }
        }
    }

    fun toggleIsSendingReports() {
        val scope = scopeVm()
        scope.launchEx {
            val newValue = !KvDb.KEY.IS_SENDING_REPORTS.selectOrNull().isSendingReports()
            KvDb.KEY.IS_SENDING_REPORTS.upsertIsSendingReports(newValue)
            reportApi("Up IS_SENDING_REPORTS: $newValue", force = true)
            if (newValue)
                defaultScope().launchEx {
                    ping(force = true)
                }
        }
    }

    //

    companion object {

        const val prayEmoji = "🙏"

        const val textHi = "Hi,"
        const val textHere = "Developer is here  $developerEmoji"
        const val textRule = "The strongest rule I follow is \"Privacy with no compromise\". I believe that task management is extremely sensitive information. No one should have access to it."
        const val textNever = "The app never sends any personal information!"
        const val textOnly = "The only thing the app sends is technical reports. That's all the data the app sends:"
        const val textKindly = "I kindly ask you 🙏 to turn on sending reports. It is the only way I can know I have such great users like you, nothing else motivates me to keep going."

        val sendItems: List<String> = listOf(
            "OS: ${deviceData.os}",
            "Device: ${deviceData.device}",
        )
    }
}
