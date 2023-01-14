package timeto.shared.vm.ui

import timeto.shared.ColorNative
import timeto.shared.TextFeatures

class DaytimeUI(
    daytime: Int,
) {
    val daytimeText = TextFeatures.daytimeToString(daytime)
    val color: ColorNative = ColorNative.blue
}
