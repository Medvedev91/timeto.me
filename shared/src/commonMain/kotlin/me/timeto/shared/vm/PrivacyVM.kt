package me.timeto.shared.vm

import kotlinx.coroutines.flow.*
import me.timeto.shared.deviceData

class PrivacyVM : __VM<PrivacyVM.State>() {

    data class State(
        var tmp: String,
    ) {

        val headerTitle: String = "Privacy"

        val text1 = PrivacyVM.text1
        val text2 = PrivacyVM.text2
        val text3 = PrivacyVM.text3
        val text4 = PrivacyVM.text4
        val text5 = PrivacyVM.text5
        val text6 = PrivacyVM.text6

        val sendItems: List<String> = PrivacyVM.sendItems
    }

    override val state = MutableStateFlow(
        State(
            tmp = "todo"
        )
    )

    //

    companion object {

        const val text1 = "Hi!"
        const val text2 = "Developer is here."
        const val text3 = "The strongest rule I follow while making the app is \"Privacy with no compromise\". I believe that task management is extremely sensitive information. No one should have access to it."
        const val text4 = "The app never sends any personal information!"
        const val text5 = "The only thing the app sends is technical reports. That's all the data the app sends:"
        const val text6 = "I kindly ask you 🙏 not to turn off sending reports. It is the only way I can know I have such great users like you, nothing else motivates me to keep going."

        val sendItems: List<String> = listOf(
            "OS: ${deviceData.os}",
            "Device: ${deviceData.device}",
        )
    }
}
