import SwiftUI
import shared

struct TimerPickerSheet: View {

    @Binding private var isPresented: Bool
    private let title: String
    private let doneText: String
    private let onDone: (_ seconds: Int) -> Void

    ///

    // Int32 to match type with TimerPickerItem.seconds
    @State private var formSeconds: Int32
    private let pickerItems: [TimerPickerItem]

    ///

    init(
            isPresented: Binding<Bool>,
            title: String,
            doneText: String,
            defMinutes: Int,
            onDone: @escaping (_ seconds: Int) -> Void
    ) {
        _isPresented = isPresented
        self.title = title
        self.doneText = doneText
        self.onDone = onDone

        ///

        let defSeconds = defMinutes.toInt32() * 60
        _formSeconds = State(initialValue: defSeconds)
        pickerItems = TimerPickerItem.companion.buildList(defSeconds: defSeconds)
    }

    var body: some View {

        VStack(spacing: 0) {

            HStack(spacing: 4) {

                Button(
                        action: { isPresented.toggle() },
                        label: { Text("Cancel") }
                )

                Spacer(minLength: 0)

                Text(title)
                        .font(.title2)
                        .fontWeight(.semibold)
                        .multilineTextAlignment(.center)

                Spacer(minLength: 0)

                Button(
                        action: {
                            onDone(formSeconds.toInt())
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

            Picker(
                    "Time",
                    selection: $formSeconds
            ) {
                ForEach(
                        pickerItems,
                        id: \.seconds
                ) { item in
                    Text(item.title)
                }
            }
                    .pickerStyle(.wheel)
                    .foregroundColor(.primary)
                    .padding(.bottom, 5)

            Spacer()
        }
    }
}
