import SwiftUI

struct PickerStateView: View {

    private let values: [String]

    @State private var formValue: String // nil does not work
    private let stateValue: String
    private let onValueChanged: (String) -> Void

    init(
            values: [String],
            value: String,
            onValueChanged: @escaping (String) -> Void
    ) {
        self.values = values
        _formValue = State(initialValue: value)
        stateValue = value
        self.onValueChanged = onValueChanged
    }

    var body: some View {
        Picker(
                selection: $formValue,
                label: Text(""),
                content: {
                    ForEach(values, id: \.self) { value in
                        Text(value).tag(value)
                    }
                }
        )
                ///
                .onChange(of: formValue) { newValue in
                    if (newValue != stateValue) {
                        onValueChanged(newValue)
                    }
                }
                .onChange(of: stateValue) { newValue in
                    formValue = newValue
                }
    }
}
