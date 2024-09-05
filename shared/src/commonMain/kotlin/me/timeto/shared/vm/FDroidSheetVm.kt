package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.KvDb
import me.timeto.shared.defaultScope
import me.timeto.shared.launchEx
import me.timeto.shared.ping
import me.timeto.shared.reportApi

class FDroidSheetVm : __Vm<FDroidSheetVm.State>() {

    data class State(
        val tmp: Int,
    ) {

        val headerTitle = "Privacy"

        val text1 = PrivacySheetVm.textHi
        val text2 = PrivacySheetVm.textHere
        val text3 = PrivacySheetVm.textRule
        val text4 = PrivacySheetVm.textNever
        val text5 = PrivacySheetVm.textOnly
        val text6 = "I kindly ask you 🙏 to allow sending reports. It is the only way I can know I have such great users like you, nothing else motivates me to keep going."

        val sendItems: List<String> = PrivacySheetVm.sendItems
    }

    override val state = MutableStateFlow(
        State(
            tmp = 0,
        )
    )

    fun setSendingReports(
        isSendReports: Boolean,
        onSuccess: () -> Unit,
    ) {
        val scope = scopeVm()
        scope.launchEx {
            KvDb.KEY.IS_SENDING_REPORTS.upsertIsSendingReports(isSendReports)
            if (isSendReports) {
                defaultScope().launchEx {
                    ping(force = true)
                }
                reportApi("F-Droid enable reports", force = true)
            }
            onSuccess()
        }
    }
}
