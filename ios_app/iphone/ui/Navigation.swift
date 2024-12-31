import SwiftUI
import shared

extension View {

    func attachNavigation() -> some View {
        modifier(NavigationModifier())
    }
}

struct NavigationLink<Content: View>: View {
    
    private let path: NavigationPath
    private let content: () -> Content

    init(
        _ path: NavigationPath,
        content: @escaping () -> Content
    ) {
        self.path = path
        self.content = content
    }
    
    var body: some View {
        SwiftUI.NavigationLink(value: path, label: content)
    }
}

class Navigation: ObservableObject {
    
    @Published var path: [NavigationPath] = []
}

enum NavigationPath: Hashable {
    case readme(defaultItem: ReadmeSheetVm.DefaultItem)
    case whatsNew
}

///

private struct NavigationModifier: ViewModifier {

    @StateObject private var navigation = Navigation()

    func body(content: Content) -> some View {
        NavigationStack(path: $navigation.path) {
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
