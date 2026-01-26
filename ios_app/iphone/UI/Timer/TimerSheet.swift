import SwiftUI
import shared

struct TimerSheet: View {
    
    let title: String
    let doneTitle: String
    let initSeconds: Int
    let hints: [Int]
    let onDone: (Int) -> Void

    var body: some View {
        VmView({
            TimerPickerVm(
                initSeconds: initSeconds.toInt32(),
                hints: hints.map { $0.toKotlinInt() },
            )
        }) { _, state in
            TimerSheetInner(
                title: title,
                doneTitle: doneTitle,
                onDone: onDone,
                hintsUi: state.hintsUi,
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
    let hintsUi: [TimerPickerVm.HintUi]
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
            
            HStack {
                ForEach(hintsUi, id: \.timer) { hintUi in
                    Button(
                        action: {
                            onDone(hintUi.timer.toInt())
                            dismiss()
                        },
                        label: {
                            Text(hintUi.title)
                                .foregroundColor(.blue)
                                .fontWeight(.semibold)
                                .padding(.horizontal, 6)
                        }
                    )
                }
            }
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
