import SwiftUI
import shared

struct ActivityFormTimerHintsSheet: View {
    
    let initTimerHints: Set<Int>
    let onDone: (Set<Int>) -> Void
    
    var body: some View {
        VmView({
            ActivityFormTimerHintsVm(
                initTimerHints: initTimerHints.toKotlin()
            )
        }) { vm, state in
            ActivityFormTimerHintsSheetInner(
                vm: vm,
                state: state,
                onDone: onDone
            )
        }
    }
}

///

private let timerSheetHeader = "Timer Hint"

private struct ActivityFormTimerHintsSheetInner: View {
    
    let vm: ActivityFormTimerHintsVm
    let state: ActivityFormTimerHintsVm.State
    
    let onDone: (Set<Int>) -> Void

    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    @State private var editMode: EditMode = .active
    
    var body: some View {
        List {
            Section {
                ForEach(state.timerHintsUi, id: \.self) { timerHintUi in
                    Button(timerHintUi.text) {
                        navigation.sheet {
                            TimerSheet(
                                title: timerSheetHeader,
                                doneTitle: "Save",
                                initSeconds: timerHintUi.seconds.toInt(),
                                onDone: { newSeconds in
                                    vm.delete(seconds: timerHintUi.seconds.toInt32())
                                    vm.add(seconds: newSeconds.toInt32())
                                }
                            )
                            .interactiveDismissDisabled()
                        }
                    }
                }
                .onDeleteVm { idx in
                    vm.delete(seconds: state.timerHintsUi[idx].seconds)
                }
            }
            .listSectionSeparator(.hidden, edges: [.top, .bottom])
        }
        .environment(\.editMode, $editMode)
        .interactiveDismissDisabled()
        .listStyle(.plain)
        .navigationTitle("Timer Hints")
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    onDone(state.timerHints.toSwift())
                    dismiss()
                }
                .fontWeight(.semibold)
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                
                BottomBarAddButton(
                    text: timerSheetHeader,
                    action: {
                        navigation.sheet {
                            TimerSheet(
                                title: "Timer Hint",
                                doneTitle: "Add",
                                initSeconds: 45 * 60,
                                onDone: { newTimer in
                                    vm.add(seconds: newTimer.toInt32())
                                }
                            )
                            .interactiveDismissDisabled()
                        }
                    }
                )
                
                Spacer()
            }
        }
    }
}
