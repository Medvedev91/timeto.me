import SwiftUI
import shared

struct HistoryFormTimeSheet: View {
    
    let initTime: Int
    let onDone: (Int) -> Void
    
    var body: some View {
        VmView({
            HistoryFormTimeVm(
                initTime: initTime.toInt32()
            )
        }) { vm, state in
            HistoryFormTimeSheetInner(
                vm: vm,
                state: state,
                selectedTime: state.initTime,
                onDone: onDone
            )
        }
    }
}

///

private struct HistoryFormTimeSheetInner: View {
    
    let vm: HistoryFormTimeVm
    let state: HistoryFormTimeVm.State
    
    @State var selectedTime: Int32
    let onDone: (Int) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack {
            Picker("", selection: $selectedTime) {
                ForEach(state.timerItemUi, id: \.time) { itemUi in
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
                    onDone(selectedTime.toInt())
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
