import SwiftUI
import shared

struct GoalFormTimerHintsSheet: View {
    
    let initTimerHints: [Int]
    let onDone: ([Int]) -> Void
    
    var body: some View {
        VmView({
            GoalFormTimerHintsVm(
                initTimerHints: initTimerHints.map { $0.toKotlinInt() },
            )
        }) { vm, state in
            GoalFormTimerHintsSheetInner(
                vm: vm,
                state: state,
                onDone: onDone,
            )
        }
    }
}

private struct GoalFormTimerHintsSheetInner: View {
    
    let vm: GoalFormTimerHintsVm
    let state: GoalFormTimerHintsVm.State
    
    let onDone: ([Int]) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    @State private var editMode: EditMode = .active
    
    var body: some View {
        List {
            
            Section {
                
                ForEach(state.timerHintsUi, id: \.seconds) { timerHintUi in
                    Text(timerHintUi.text)
                }
                .onDeleteVm { idx in
                    vm.delete(seconds: state.timerHintsUi[idx].seconds)
                }
                
                Button("New Timer Hint") {
                    navigation.sheet {
                        TimerSheet(
                            title: "Timer",
                            doneTitle: "Done",
                            initSeconds: 45 * 60,
                            onDone: { newTimerSeconds in
                                vm.add(seconds: newTimerSeconds.toInt32())
                            }
                        )
                    }
                }
                .foregroundColor(.blue)
                .font(.system(size: HomeScreen__primaryFontSize))
                .frame(maxWidth: .infinity, alignment: .leading)
                .frame(height: HomeScreen__itemHeight)
                .padding(.leading, H_PADDING)
                .textAlign(.leading)
                .customListItem()
            }
            .listSectionSeparator(.hidden, edges: [.top, .bottom])
        }
        .environment(\.editMode, $editMode)
        .contentMargins(.vertical, 8)
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
                    onDone(vm.getTimerHints().map { $0.toInt() })
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
