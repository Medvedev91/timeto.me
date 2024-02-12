package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.KvDb
import me.timeto.shared.db.KvDb.Companion.isSendingReports
import me.timeto.shared.deviceData
import me.timeto.shared.launchEx
import me.timeto.shared.onEachExIn

class PrivacySheetVM : __VM<PrivacySheetVM.State>() {

    data class State(
        val isSendReportsEnabled: Boolean,
    ) {

        val headerTitle = "Privacy"

        val sendReportsTitle = "Send Reports${if (isSendReportsEnabled) "" else "  $prayEmoji"}"

        val text1 = PrivacySheetVM.textHi
        val text2 = PrivacySheetVM.textHere
        val text3 = PrivacySheetVM.textRule
        val text4 = PrivacySheetVM.textNever
        val text5 = PrivacySheetVM.textOnly
        val text6 = PrivacySheetVM.textKindly

        val sendItems: List<String> = PrivacySheetVM.sendItems
    }

    override val state = MutableStateFlow(
        State(
            isSendReportsEnabled = false, // todo init value
        )
    )

    override fun onAppear() {
        val scope = scopeVM()
        KvDb.KEY.IS_SENDING_REPORTS.getOrNullFlow().onEachExIn(scope) { kvDb ->
            state.update {
                val newVal = kvDb?.value.isSendingReports()
                it.copy(isSendReportsEnabled = newVal)
            }
        }
    }

    fun toggleIsSendingReports() {
        val scope = scopeVM()
        scope.launchEx {
            val cur = KvDb.KEY.IS_SENDING_REPORTS.selectOrNull().isSendingReports()
            KvDb.KEY.IS_SENDING_REPORTS.upsertBool(!cur)
        }
    }

    //

    companion object {

        const val prayEmoji = "🙏"

        const val textHi = "Hi!"
        const val textHere = "Developer is here."
        const val textRule = "The strongest rule I follow while making the app is \"Privacy with no compromise\". I believe that task management is extremely sensitive information. No one should have access to it."
        const val textNever = "The app never sends any personal information!"
        const val textOnly = "The only thing the app sends is technical reports. That's all the data the app sends:"
        const val textKindly = "I kindly ask you 🙏 not to turn off sending reports. It is the only way I can know I have such great users like you, nothing else motivates me to keep going."

        val sendItems: List<String> = listOf(
            "OS: ${deviceData.os}",
            "Device: ${deviceData.device}",
        )
    }
}
