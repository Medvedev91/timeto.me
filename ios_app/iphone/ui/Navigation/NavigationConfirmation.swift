import SwiftUI

struct NavigationConfirmation: View, Identifiable {
    
    let id: String = UUID().uuidString
    
    let message: String
    let buttonText: String
    let onConfirm: () -> Void
    
    let onRemove: (_ id: String) -> Void
    
    @State private var isPresented = true
    
    var body: some View {
        ZStack {}
            .alert(
                message,
                isPresented: $isPresented,
                actions: {
                    Button("Cancel", role: .cancel) {
                        onRemove(id)
                    }
                    Button(buttonText, role: .destructive) {
                        onConfirm()
                        onRemove(id)
                    }
                }
            )
    }
}
