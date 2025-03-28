import SwiftUI
import shared

struct DaytimePickerSheet: View {
    
    private let title: String
    private let doneText: String
    private let onPick: (_ daytimeUi: DaytimeUi) -> Void
    private let onRemove: () -> Void
    
    ///
    
    @State private var dateTrick: Date
    
    @Environment(\.dismiss) private var dismiss

    ///
    
    init(
        title: String,
        doneText: String,
        daytimeUi: DaytimeUi,
        onPick: @escaping (_ daytimeUi: DaytimeUi) -> Void,
        onRemove: @escaping () -> Void
    ) {
        self.title = title
        self.doneText = doneText
        self.onPick = onPick
        self.onRemove = onRemove
        ///
        _dateTrick = State(initialValue: Date().startOfDay().inSeconds((daytimeUi.seconds).toInt()))
    }
    
    var body: some View {
        
        VStack(spacing: 0) {
            
            HStack(spacing: 4) {
                
                Button(
                    action: { dismiss() },
                    label: { Text("Cancel") }
                )
                
                Spacer()
                
                Text(title)
                    .font(.title2)
                    .fontWeight(.semibold)
                    .multilineTextAlignment(.center)
                
                Spacer()
                
                Button(
                    action: {
                        let calendar = Calendar.current
                        let newDaytimeUi = DaytimeUi(
                            hour: calendar.component(.hour, from: dateTrick).toInt32(),
                            minute: calendar.component(.minute, from: dateTrick).toInt32()
                        )
                        onPick(newDaytimeUi)
                        dismiss()
                    },
                    label: {
                        Text(doneText)
                            .fontWeight(.bold)
                    }
                )
            }
            .padding(.horizontal, 25)
            .padding(.top, 24)
            
            Spacer()
            
            DatePicker(
                "Start Date",
                selection: $dateTrick,
                displayedComponents: [.hourAndMinute]
            )
            .labelsHidden()
            .datePickerStyle(.wheel)
            
            Button("Remove") {
                onRemove()
                dismiss()
            }
            .foregroundColor(.red)
            
            Spacer()
        }
    }
}
