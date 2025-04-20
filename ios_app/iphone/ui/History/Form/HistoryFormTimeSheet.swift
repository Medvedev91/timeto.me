import SwiftUI
import shared

struct HistoryFormTimeSheet: View {
    
    let strategy: HistoryFormTimeStrategy
    
    var body: some View {
        VmView({
            HistoryFormTimeVm(
                strategy: strategy
            )
        }) { vm, state in
            HistoryFormTimeSheetInner(
                vm: vm,
                state: state,
                strategy: strategy,
                selectedTime: state.initTime
            )
        }
    }
}

///

private struct HistoryFormTimeSheetInner: View {
    
    let vm: HistoryFormTimeVm
    let state: HistoryFormTimeVm.State
    
    let strategy: HistoryFormTimeStrategy
    @State var selectedTime: Int32

    ///
    
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack {
            Picker("", selection: $selectedTime) {
                ForEach(state.timerItemsUi, id: \.time) { itemUi in
                    Text(itemUi.title)
                }
            }
            .pickerStyle(.wheel)
        }
        .interactiveDismissDisabled()
        .navigationTitle("Time")
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    if let strategy = strategy as? HistoryFormTimeStrategy.Picker {
                        strategy.onDone(selectedTime.toKotlinInt())
                        dismiss()
                    }
                    else {
                        fatalError()
                    }
                }
                .fontWeight(.semibold)
            }
        }
    }
}
