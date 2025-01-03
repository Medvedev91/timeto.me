import SwiftUI
import shared

extension View {

    func attachNavigation() -> some View {
        modifier(NavigationModifier())
    }
}

@MainActor
class Navigation: ObservableObject {
    
    @Published fileprivate var pathList: [NavigationPath] = []
    @Published fileprivate var sheetViews = [NavigationSheet<AnyView>]()

    func push(_ path: NavigationPath) {
        pathList.append(path)
    }
    
    func sheet<Content: View>(
        @ViewBuilder content: @escaping () -> Content
    ) {
        sheetViews.append(
            NavigationSheet(
                content: {
                    AnyView(content())
                },
                onRemove: { id in
                    self.sheetViews.removeAll { $0.id == id }
                }
            )
        )
    }
}

///

private struct NavigationModifier: ViewModifier {

    @StateObject private var navigation = Navigation()

    func body(content: Content) -> some View {
        
        ZStack {
            
            NavigationStack(path: $navigation.pathList) {
                content.navigationDestination(for: NavigationPath.self) { value in
                    AnyView(value.rend())
                }
            }
            
            ForEach(navigation.sheetViews) { sheetView in
                sheetView
            }
        }
        .environmentObject(navigation)
    }
}
