import SwiftUI
import shared

struct TimerSheet: View {
    
    let title: String
    let doneTitle: String
    let initSeconds: Int
    let onDone: (Int) -> Void

    var body: some View {
        VmView({
            TimerPickerVm(
                initSeconds: initSeconds.toInt32()
            )
        }) { _, state in
            TimerSheetInner(
                title: title,
                doneTitle: doneTitle,
                onDone: onDone,
                pickerItemsUi: state.pickerItemsUi,
                selected: initSeconds.toInt32()
            )
        }
    }
}

private struct TimerSheetInner: View {
    
    let title: String
    let doneTitle: String
    let onDone: (Int) -> Void
    let pickerItemsUi: [TimerPickerVm.PickerItemUi]
    
    @State var selected: Int32

    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        VStack {
            
            Spacer()
            
            Picker("", selection: $selected) {
                ForEach(pickerItemsUi, id: \.seconds) { itemUi in
                    Text(itemUi.title)
                }
            }
            .pickerStyle(.wheel)
            .foregroundColor(.primary)
            .padding(.bottom, 5)
            
            Spacer()
        }
        .presentationDetents([.height(350)])
        .navigationTitle(title)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(doneTitle) {
                    onDone(selected.toInt())
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
