import SwiftUI
import shared

enum NavigationPath: Hashable {
    
    case readme(defaultItem: ReadmeVm.DefaultItem)
    case whatsNew
    case checklist(checklistDb: ChecklistDb, maxLines: Int, onDelete: () -> Void)
    case note(noteDb: NoteDb, onDelete: () -> Void)
    case privacy
    
    func hash(into hasher: inout Hasher) {
        switch self {
        case .readme(let defaultItem):
            hasher.combine("readme")
            hasher.combine(defaultItem)
        case .whatsNew:
            hasher.combine("whats_new")
        case .checklist(let checklistDb, _, _):
            hasher.combine("checklist")
            hasher.combine(checklistDb.id)
        case .note(let noteDb, _):
            hasher.combine("note")
            hasher.combine(noteDb.id)
        case .privacy:
            hasher.combine("privacy")
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
        case .note(let noteDb, let onDelete):
            NoteScreen(noteDb: noteDb, onDelete: onDelete)
        case .privacy:
            PrivacyScreen(titleDisplayMode: .inline)
        }
    }
}
