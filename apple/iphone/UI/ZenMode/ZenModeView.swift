import SwiftUI
import shared

private let timerFontFullScreen = buildTimerFont(size: 72)
private let timerFontHalfScreen = buildTimerFont(size: 56)

struct ZenModeView: View {
    
    var body: some View {
        VmView({ ZenModeVm() }) { vm, _ in
            let state = vm.state.value as! ZenModeVm.State
            ZenModeViewLocal(
                vm: vm,
                state: state,
                showChecklist: state.initShowChecklist,
            )
        }
    }
}

///

private let notePadding: CGFloat = 16
private let noteFontSize: CGFloat = 24
private let dateFontSize: CGFloat = 18

private struct ZenModeViewLocal: View {
    
    let vm: ZenModeVm
    let state: ZenModeVm.State
    
    @State var showChecklist: Bool
    
    ///
    
    @State private var showControls = true
    @State private var hideControlsTask: Task<(), Never>?
    
    var body: some View {
        GeometryReader { geometry in
            
            let checklistDb: ChecklistDb? = showChecklist ? state.checklistDb : nil
            
            let fullWidth: CGFloat = geometry.size.width
            let checklistWidth: CGFloat = checklistDb == nil ? 0 : fullWidth * 0.35
            let timerWidth: CGFloat = fullWidth - checklistWidth
            
            HStack {
                
                ZStack {
                    
                    VStack {
                        
                        Text(state.dateText)
                            .font(.system(size: dateFontSize, weight: .semibold))
                            .foregroundColor(.secondary)
                        
                        Spacer()
                        
                        if state.checklistDb != nil {
                            Button(showChecklist ? "Hide Checklist" : "Show Checklist") {
                                showChecklist ? vm.hideChecklist() : vm.showChecklist()
                                scheduleHideControls()
                                withAnimation {
                                    showChecklist.toggle()
                                }
                            }
                            .font(.system(size: dateFontSize, weight: .semibold))
                            .foregroundColor(.secondary)
                        }
                    }
                    .padding(.top, 16)
                    .opacity(showControls ? 1 : 0)
                    .zIndex(2)
                    
                    VStack {
                        
                        Text(state.timerStateUi.note)
                            .font(.system(size: noteFontSize, weight: .semibold))
                            .foregroundColor(state.timerStateUi.noteColor.toColor())
                            .padding(.bottom, notePadding)
                            .opacity(showControls ? 1 : 0)
                        
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
                        
                        Text("--Hidden Padding--")
                            .opacity(0)
                            .padding(.top, notePadding)
                            .font(.system(size: noteFontSize, weight: .semibold))
                    }
                    .zIndex(1)
                }
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
        .onTapGesture {
            if showControls {
                hideControlsTask?.cancel()
                withAnimation {
                    showControls = false
                }
            } else {
                scheduleHideControls()
                withAnimation {
                    showControls = true
                }
            }
        }
        .onAppear {
            scheduleHideControls(nanoseconds: 1_000_000_000)
        }
    }
    
    private func scheduleHideControls(
        nanoseconds: UInt64 = 3_000_000_000,
    ) {
        hideControlsTask?.cancel()
        hideControlsTask = Task {
            do {
                try await Task.sleep(nanoseconds: nanoseconds)
                withAnimation {
                    showControls = false
                }
            } catch {
            }
        }
    }
}
