import SwiftUI
import shared

struct ChecklistScreen: View {
    
    let checklistDb: ChecklistDb
    let maxLines: Int
    let onDelete: () -> Void
    
    var body: some View {
        
        ChecklistView(
            checklistDb: checklistDb,
            maxLines: maxLines,
            onDelete: onDelete
        )
        .contentMarginsTabBar(extra: 12)
        .navigationTitle(checklistDb.name)
        .toolbarTitleDisplayMode(.inline)
    }
}
