import SwiftUI
import shared

private let timerFontFullScreen = buildTimerFont(size: 72)
private let timerFontHalfScreen = buildTimerFont(size: 56)

struct ZenModeView: View {
    
    var body: some View {
        VmView({ ZenModeVm() }) { vm, _ in
            let state = vm.state.value as! ZenModeVm.State
            ZenModeViewLocal(
                state: state,
            )
        }
    }
}

private struct ZenModeViewLocal: View {
    
    let state: ZenModeVm.State
    
    var body: some View {
        GeometryReader { geometry in
            
            let checklistDb: ChecklistDb? = state.checklistDb

            let fullWidth: CGFloat = geometry.size.width
            let checklistWidth: CGFloat = checklistDb == nil ? 0 : fullWidth * 0.35
            let timerWidth: CGFloat = fullWidth - checklistWidth
            
            HStack {
                
                Button(
                    action: {
                        state.timerStateUi.togglePomodoro()
                    },
                    label: {
                        Text(state.timerStateUi.timerText)
                            .font(checklistDb == nil ? timerFontFullScreen : timerFontHalfScreen)
                            .foregroundColor(state.timerStateUi.timerColor.toColor())
                            .lineLimit(1)
                            .fixedSize()
                    },
                )
                .frame(width: timerWidth)
                
                if let checklistDb = checklistDb {
                    ChecklistView(
                        checklistDb: checklistDb,
                        maxLines: 1,
                        fullHeight: false,
                        withAddButton: false,
                        onDelete: {},
                    )
                    .frame(width: checklistWidth)
                }
            }
            .frame(height: geometry.size.height)
        }
        .fillMaxSize()
        .background(.black)
    }
}
