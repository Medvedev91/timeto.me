import SwiftUI
import shared

extension View {

    func attachNavigation() -> some View {
        modifier(NavigationModifier())
    }
}

@MainActor
class Navigation: ObservableObject {
    
    @Published var pathList: [NavigationPath] = []
    
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
                switch value {
                case .readme(let defaultItem):
                    ReadmeScreen(defaultItem: defaultItem)
                case .whatsNew:
                    WhatsNewScreen()
                }
            }
        }
        .environmentObject(navigation)
    }
}
