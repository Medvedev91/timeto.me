import SwiftUI
import shared

enum NavigationPath: Hashable {
    
    case readme(defaultItem: ReadmeVm.DefaultItem)
    case whatsNew
    case checklist(checklistDb: ChecklistDb, maxLines: Int, onDelete: () -> Void)
    
    func hash(into hasher: inout Hasher) {
        switch self {
        case .readme(let defaultItem):
            hasher.combine("readme")
            hasher.combine(defaultItem)
        case .whatsNew:
            hasher.combine("whatsNew")
        case .checklist(let checklistDb, let maxLines, _):
            hasher.combine("checklist")
            hasher.combine(checklistDb)
            hasher.combine(maxLines)
        }
    }
    
    static func == (lhs: NavigationPath, rhs: NavigationPath) -> Bool {
        lhs.hashValue == rhs.hashValue
    }
}

extension NavigationPath {
    
    func rend() -> any View {
        switch self {
        case .readme(let defaultItem):
            ReadmeScreen(defaultItem: defaultItem)
        case .whatsNew:
            WhatsNewScreen()
        case .checklist(let checklistDb, let maxLines, let onDelete):
            ChecklistScreen(checklistDb: checklistDb, maxLines: maxLines, onDelete: onDelete)
        }
    }
}
