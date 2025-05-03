import SwiftUI
import shared

extension Navigation {
    
    func showActivityTimerSheet(
        activityDb: ActivityDb,
        strategy: ActivityTimerStrategy,
        // false for nested sheet to speed up closing
        hideOnStart: Bool,
        onStart: @escaping () -> Void
    ) {
        self.sheet {
            VmView({
                ActivityTimerVm(
                    activityDb: activityDb,
                    strategy: strategy
                )
            }) { vm, state in
                ActivityTimerSheet(
                    vm: vm,
                    state: state,
                    selectedSeconds: state.initSeconds,
                    onStart: onStart
                )
            }
        }
    }
}
 
private struct ActivityTimerSheet: View {
    
    let vm: ActivityTimerVm
    let state: ActivityTimerVm.State
    
    @State var selectedSeconds: Int32
    let onStart: () -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        VStack {
            
            if let note = state.note {
                Text(note)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
            
            Spacer()
            
            Picker("", selection: $selectedSeconds) {
                ForEach(state.timerItemsUi, id: \.seconds) { itemUi in
                    Text(itemUi.title)
                }
            }
            .pickerStyle(.wheel)
            .foregroundColor(.primary)
            .padding(.bottom, state.note != nil ? 30 : 5)
            
            Spacer()
        }
        .presentationDetents([.height(400)])
        .presentationDragIndicator(.visible)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Start") {
                    vm.start(
                        seconds: selectedSeconds,
                        onSuccess: {
                            onStart()
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
            }
        }
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
    }
}
