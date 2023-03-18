package timeto.shared.vm.ui

import timeto.shared.ColorNative
import timeto.shared.TextFeatures
import timeto.shared.launchExDefault

class TextFeaturesTriggersUI(
    val textFeatures: TextFeatures,
) {

    val triggers: List<Trigger>

    init {
        val triggersTmp = mutableListOf<Trigger>()
        textFeatures.checklists.forEach { checklist ->
            triggersTmp.add(Trigger(
                id = "c${checklist.id}",
                title = checklist.name,
                color = ColorNative.green,
                performUI = { launchExDefault { checklist.performUI() } }
            ))
        }
        textFeatures.shortcuts.forEach { shortcut ->
            triggersTmp.add(Trigger(
                id = "s${shortcut.id}",
                title = shortcut.name,
                color = ColorNative.red,
                performUI = { launchExDefault { shortcut.performUI() } }
            ))
        }
        triggers = triggersTmp
    }

    class Trigger(
        val id: String,
        val title: String,
        val color: ColorNative,
        val performUI: () -> Unit,
    )
}
