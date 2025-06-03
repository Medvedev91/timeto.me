import SwiftUI
import shared

struct HomeSettingsFullScreen: View {
    
    var body: some View {
        HomeSettingsFullScreenInner()
    }
}

private struct HomeSettingsFullScreenInner: View {
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        VStack {
            Spacer()
            HomeSettingsButtonsView()
        }
        .navigationTitle("Home Settings")
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Save") {
                    // todo
                }
                .fontWeight(.semibold)
            }
        }
    }
}
