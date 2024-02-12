package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.db.KvDb
import me.timeto.shared.launchEx
import me.timeto.shared.reportApi

class FDroidSheetVM : __VM<FDroidSheetVM.State>() {

    data class State(
        val tmp: Int,
    ) {

        val headerTitle = "Privacy"

        val text1 = PrivacySheetVM.textHi
        val text2 = PrivacySheetVM.textHere
        val text3 = PrivacySheetVM.textRule
        val text4 = PrivacySheetVM.textNever
        val text5 = PrivacySheetVM.textOnly
        val text6 = "I kindly ask you 🙏 to allow sending reports. It is the only way I can know I have such great users like you, nothing else motivates me to keep going."

        val sendItems: List<String> = PrivacySheetVM.sendItems
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
        val scope = scopeVM()
        scope.launchEx {
            KvDb.KEY.IS_SENDING_REPORTS.upsertBool(isSendReports)
            if (isSendReports)
                reportApi("F-Droid enable reports")
            onSuccess()
        }
    }
}
