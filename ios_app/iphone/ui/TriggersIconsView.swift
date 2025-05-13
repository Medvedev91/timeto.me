import SwiftUI
import shared

struct TriggersIconsView: View {
    
    let checklistsDb: [ChecklistDb]
    let shortcutsDb: [ShortcutDb]
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        if checklistsDb.isEmpty && shortcutsDb.isEmpty {
            EmptyView()
        } else {
            HStack(spacing: 8) {
                ForEach(shortcutsDb, id: \.id) { shortcutDb in
                    IconView(iconType: .shortcut, onClick: {
                        shortcutDb.performUi()
                    })
                }
                ForEach(checklistsDb, id: \.id) { checklistDb in
                    IconView(iconType: .checklist, onClick: {
                        navigation.push(
                            .checklist(
                                checklistDb: checklistDb,
                                maxLines: .max,
                                onDelete: {}
                            )
                        )
                    })
                }
            }
        }
    }
}

private struct IconView: View {
    
    let iconType: IconType
    let onClick: () -> Void
    
    ///

    private var iconName: String {
        switch iconType {
        case .checklist: "checkmark.circle.fill"
        case .shortcut: "arrow.up.forward.circle.fill"
        }
    }
    
    private var iconColor: Color {
        switch iconType {
        case .checklist: .green
        case .shortcut: .red
        }
    }
    
    var body: some View {
        Button(
            action: {
                onClick()
            },
            label: {
                Image(systemName: iconName)
                    .foregroundColor(iconColor)
            }
        )
    }
}

private enum IconType {
    case checklist
    case shortcut
}
