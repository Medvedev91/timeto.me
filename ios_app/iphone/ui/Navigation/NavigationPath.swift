import SwiftUI
import shared

enum NavigationPath: Hashable {
    
    case readme(defaultItem: ReadmeVm.DefaultItem)
    case whatsNew
}

extension NavigationPath {
    
    func rend() -> any View {
        switch self {
        case .readme(let defaultItem):
            ReadmeScreen(defaultItem: defaultItem)
        case .whatsNew:
            WhatsNewScreen()
        }
    }
}
