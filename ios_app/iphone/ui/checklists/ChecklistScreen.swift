import SwiftUI
import shared

struct ChecklistScreen: View {
    
    let checklistDb: ChecklistDb
    let maxLines: Int
    let onDelete: () -> Void
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        VmView({
            ChecklistScreenVm(
                checklistDb: checklistDb
            )
        }) { vm, state in
            
            ChecklistView(
                checklistDb: state.checklistDb,
                maxLines: maxLines,
                withAddButton: true,
                onDelete: onDelete
            )
            .contentMarginsTabBar(extra: 12)
            .navigationTitle(state.checklistDb.name)
            .toolbarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button("Edit") {
                        navigation.sheet {
                            ChecklistFormSheet(
                                checklistDb: checklistDb,
                                onSave: { _ in },
                                onDelete: {
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
