import SwiftUI

struct GoalFormFinishedTextSheet: View {
    
    @State var text: String
    let onDone: (String) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @FocusState private var isFocused: Bool

    var body: some View {
        List {
            TextField("", text: $text)
                .focused($isFocused)
                .onAppear {
                    isFocused = true
                }
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .navigationTitle("Finished Text")
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    onDone(text)
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
