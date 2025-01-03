import SwiftUI

class NativeSheet: ObservableObject {
    
    @Published fileprivate var items = [NativeSheetItem<AnyView>]()
    
    func show<Content: View>(
        @ViewBuilder content: @escaping (Binding<Bool>) -> Content
    ) {
        items.append(
            NativeSheetItem(
                content: { isPresented in
                    AnyView(content(isPresented))
                }
            )
        )
    }
}

extension View {
    
    func attachNativeSheet() -> some View {
        modifier(NativeSheetModifier())
    }
}

///

private struct NativeSheetItem<Content>: View, Identifiable where Content: View {
    
    @ViewBuilder var content: (Binding<Bool>) -> Content
    
    let id = UUID().uuidString
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    @State private var isPresented = false
    
    var body: some View {
        
        ZStack {}
            .sheetEnv(
                isPresented: $isPresented,
                onDismiss: {
                    nativeSheet.items.removeAll { $0.id == id }
                }
            ) {
                content($isPresented)
            }
            .onAppear {
                isPresented = true
            }
    }
}

private struct NativeSheetModifier: ViewModifier {
    
    @StateObject private var nativeSheet = NativeSheet()
    
    func body(content: Content) -> some View {
        ZStack {
            content
            ForEach(nativeSheet.items) { wrapper in
                wrapper
            }
        }
        .environmentObject(nativeSheet)
    }
}
