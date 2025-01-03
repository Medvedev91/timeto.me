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
    
    func push(_ path: NavigationPath) {
        pathList.append(path)
    }
}

///

private struct NavigationModifier: ViewModifier {

    @StateObject private var navigation = Navigation()

    func body(content: Content) -> some View {
        NavigationStack(path: $navigation.pathList) {
            content.navigationDestination(for: NavigationPath.self) { value in
                AnyView(value.rend())
            }
        }
        .environmentObject(navigation)
    }
}
