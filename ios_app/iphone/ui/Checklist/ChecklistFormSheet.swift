import SwiftUI
import shared

struct ChecklistFormSheet: View {
    
    let checklistDb: ChecklistDb
    let onDelete: () -> Void

    var body: some View {
        VmView({
            ChecklistFormVm(checklistDb: checklistDb)
        }) { vm, state in
            ChecklistFormSheetInner(
                vm: vm,
                state: state,
                onDelete: onDelete
            )
        }
    }
}

private struct ChecklistFormSheetInner: View {
    
    let vm: ChecklistFormVm
    let state: ChecklistFormVm.State
    
    let onDelete: () -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @State private var editMode: EditMode = .active
    
    var body: some View {
        
        List {
            
            Section {
                
                ForEach(state.checklistItemsUi, id: \.checklistItemDb.id) { checklistItemUi in
                    
                    HStack(spacing: 8) {
                        
                        Button(
                            action: {
                                navigation.sheet {
                                    ChecklistItemFormSheet(
                                        checklistDb: state.checklistDb,
                                        checklistItemDb: checklistItemUi.checklistItemDb
                                    )
                                }
                            },
                            label: {
                                Image(systemName: "pencil")
                                    .font(.system(size: 16))
                                    .foregroundColor(.blue)
                            }
                        )
                        .buttonStyle(.plain)
                        .padding(.leading, 8)
                        
                        Button(
                            action: {
                                vm.down(itemUi: checklistItemUi)
                            },
                            label: {
                                Image(systemName: "arrow.down")
                                    .font(.system(size: 15))
                                    .foregroundColor(.blue)
                            }
                        )
                        .buttonStyle(.plain)
                        .padding(.leading, 8)
                        
                        Button(
                            action: {
                                vm.up(itemUi: checklistItemUi)
                            },
                            label: {
                                Image(systemName: "arrow.up")
                                    .font(.system(size: 15))
                                    .foregroundColor(.blue)
                            }
                        )
                        .buttonStyle(.plain)
                        .padding(.leading, 6)
                    }
                }
                .onDelete { indexSet in
                    for idx in indexSet {
                        vm.deleteItem(itemDb: state.checklistItemsUi[idx].checklistItemDb)
                    }
                }
            }
            .listSectionSeparator(.hidden, edges: [.top, .bottom])
        }
        .environment(\.editMode, $editMode)
        .contentMargins(.vertical, 8)
        .plainList()
        .navigationTitle(state.checklistName)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            
            ToolbarItem(placement: .topBarLeading) {
                
                Button("Settings") {
                    navigation.sheet {
                        ChecklistSettingsSheet(
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
                            
                            Text(state.newItemButtonText)
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
