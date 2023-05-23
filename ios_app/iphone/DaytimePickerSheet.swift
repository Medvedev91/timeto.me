import SwiftUI
import shared

struct DaytimePickerSheet: View {

    @Binding private var isPresented: Bool
    private let title: String
    private let doneText: String
    private let onPick: (_ seconds: Int32?) -> Void

    ///

    @State private var dateTrick: Date

    ///

    init(
            isPresented: Binding<Bool>,
            title: String,
            doneText: String,
            defMinute: Int32,
            defHour: Int32,
            onPick: @escaping (_ seconds: Int32?) -> Void
    ) {
        _isPresented = isPresented
        self.title = title
        self.doneText = doneText
        self.onPick = onPick
        ///
        _dateTrick = State(initialValue: Date().startOfDay().inSeconds((defHour * 3600 + defMinute * 60).toInt()))
    }

    var body: some View {

        VStack(spacing: 0) {

            HStack(spacing: 4) {

                Button(
                        action: { isPresented.toggle() },
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
                            let hour = calendar.component(.hour, from: dateTrick)
                            let minutes = calendar.component(.minute, from: dateTrick)
                            onPick(Int32((hour * 3_600) + (minutes * 60)))
                            isPresented = false
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
                onPick(nil)
                isPresented = false
            }
                    .foregroundColor(.red)

            Spacer()
        }
    }
}
