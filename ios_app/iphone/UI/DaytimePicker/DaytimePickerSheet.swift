import SwiftUI
import shared

struct DaytimePickerSheet: View {
    
    private let title: String
    private let doneText: String
    private let onDone: (DaytimeUi) -> Void
    private let onRemove: (() -> Void)?
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    @State private var date: Date

    init(
        title: String,
        doneText: String,
        daytimeUi: DaytimeUi,
        onDone: @escaping (DaytimeUi) -> Void,
        onRemove: (() -> Void)?
    ) {
        self.title = title
        self.doneText = doneText
        self.onDone = onDone
        self.onRemove = onRemove
        _date = State(initialValue: Date().startOfDay().inSeconds(daytimeUi.seconds.toInt()))
    }
    
    var body: some View {
        VStack {
            
            Spacer()
            
            DatePicker(
                "Start Date",
                selection: $date,
                displayedComponents: [.hourAndMinute]
            )
            .labelsHidden()
            .datePickerStyle(.wheel)
            
            Spacer()
            
            if let onRemove = onRemove {
                Button("Remove") {
                    onRemove()
                    dismiss()
                }
                .foregroundColor(.red)
                Spacer()
            }
        }
        .presentationDetents([.medium])
        .presentationDragIndicator(.visible)
        .navigationTitle(title)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(doneText) {
                    let calendar = Calendar.current
                    let newDaytimeUi = DaytimeUi(
                        hour: calendar.component(.hour, from: date).toInt32(),
                        minute: calendar.component(.minute, from: date).toInt32()
                    )
                    onDone(newDaytimeUi)
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
