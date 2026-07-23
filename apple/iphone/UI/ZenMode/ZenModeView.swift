import SwiftUI
import shared

private let timerFont = buildTimerFont(size: 72)

struct ZenModeView: View {
    
    var body: some View {
        VmView({ ZenModeVm() }) { vm, _ in
            let state = vm.state.value as! ZenModeVm.State
            ZStack {
                Button(
                    action: {
                        state.timerStateUi.togglePomodoro()
                    },
                    label: {
                        Text(state.timerStateUi.timerText)
                            .font(timerFont)
                            .foregroundColor(state.timerStateUi.timerColor.toColor())
                            .lineLimit(1)
                            .fixedSize()
                    },
                )
            }
            .fillMaxSize()
            .background(.black)
        }
    }
}
