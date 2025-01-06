import SwiftUI

struct NavigationAlert: View, Identifiable {
    
    let id: String = UUID().uuidString
    let message: String
    let onRemove: (_ id: String) -> Void
    
    @State private var isPresented = true
    
    var body: some View {
        ZStack {}
            .alert(
                "",
                isPresented: $isPresented,
                actions: {
                    Button("Ok", role: .cancel) {
                        onRemove(id)
                    }
                },
                message: {
                    Text(message)
                }
            )
    }
}
