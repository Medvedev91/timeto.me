import SwiftUI

struct NavigationSheet<Content>: View, Identifiable where Content: View {
    
    @ViewBuilder var content: () -> Content
    let onRemove: (_ id: String) -> Void
    
    let id: String = UUID().uuidString
    
    ///
    
    @EnvironmentObject private var navigation: Navigation
    @State private var isPresented = false
    
    var body: some View {
        
        ZStack {}
            .sheetEnv(
                isPresented: $isPresented,
                onDismiss: {
                    onRemove(id)
                }
            ) {
                content()
            }
            .onAppear {
                isPresented = true
            }
    }
}
