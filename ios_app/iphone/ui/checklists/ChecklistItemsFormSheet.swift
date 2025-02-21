import SwiftUI
import shared

struct ChecklistItemsFormSheet: View {
    
    let checklistDb: ChecklistDb
    let onDelete: () -> Void

    var body: some View {
        VmView({
            ChecklistItemsFormVm(checklistDb: checklistDb)
        }) { vm, state in
            ChecklistItemsFormSheetInner(
                vm: vm,
                state: state,
                onDelete: onDelete
            )
        }
    }
}

private struct ChecklistItemsFormSheetInner: View {
    
    let vm: ChecklistItemsFormVm
    let state: ChecklistItemsFormVm.State
    
    let onDelete: () -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @State private var editMode: EditMode = .active
    
    var body: some View {
        
        List {
            
            Section {
                
                ForEach(state.checklistItemsDb, id: \.id) { checklistItemDb in
                    
                    Button(
                        action: {
                            navigation.sheet {
                                ChecklistItemFormSheet(
                                    checklistDb: state.checklistDb,
                                    checklistItemDb: checklistItemDb
                                )
                            }
                        },
                        label: {
                            Text(checklistItemDb.text)
                        }
                    )
                }
                .onMoveVm { fromIdx, toIdx in
                    vm.moveIos(fromIdx: fromIdx, toIdx: toIdx)
                }
                .onDeleteVm { idx in
                    vm.deleteItem(itemDb: state.checklistItemsDb[idx])
                }
            }
            .listSectionSeparator(.hidden, edges: [.top, .bottom])
        }
        .environment(\.editMode, $editMode)
        .contentMargins(.vertical, 8)
        .listStyle(.plain)
        .navigationTitle(state.checklistName)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            
            ToolbarItem(placement: .topBarLeading) {
                
                Button("Settings") {
                    navigation.sheet {
                        ChecklistFormSheet(
                            checklistDb: state.checklistDb,
                            onSave: { _ in },
                            onDelete: {
                                dismiss()
                                onDelete()
                            }
                        )
                    }
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                
                Button("Done") {
                    if vm.isDoneAllowed(
                        dialogsManager: navigation
                    ) {
                        dismiss()
                    }
                }
                .fontWeight(.bold)
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                
                Button(
                    action: {
                        navigation.sheet {
                            ChecklistItemFormSheet(
                                checklistDb: state.checklistDb,
                                checklistItemDb: nil
                            )
                        }
                    },
                    label: {
                        
                        HStack(spacing: 8) {
                            
                            Image(systemName: "plus.circle.fill")
                                .foregroundStyle(.blue)
                                .fontWeight(.bold)
                            
                            Text(state.newItemText)
                                .foregroundColor(.blue)
                                .fontWeight(.bold)
                        }
                    }
                )
                .buttonStyle(.plain)
                
                Spacer()
            }
        }
    }
}
