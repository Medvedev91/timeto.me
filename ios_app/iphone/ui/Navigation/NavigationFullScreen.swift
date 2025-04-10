import SwiftUI

struct NavigationFullScreen<Content>: View, Identifiable where Content: View {
    
    @ViewBuilder var content: () -> Content
    let onRemove: (_ id: String) -> Void
    
    let id: String = UUID().uuidString
    
    ///
    
    @Environment(Navigation.self) private var navigation
    @State private var isPresented = false
    
    var body: some View {
        
        ZStack {}
            .fullScreenCover(
                isPresented: $isPresented,
                onDismiss: {
                    onRemove(id)
                }
            ) {
                content()
                    .attachNavigation()
            }
            .onAppear {
                isPresented = true
            }
    }
}
