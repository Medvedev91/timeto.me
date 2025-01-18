import SwiftUI
import shared

struct ChecklistItemsFormSheet: View {
    
    let checklistDb: ChecklistDb
    let onDelete: () -> Void

    var body: some View {
        VmView({
            ChecklistFormVm(checklistDb: checklistDb)
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
                .onMove { from, to in
                    from.forEach { fromIdx in
                        // Fix crash if single time
                        if fromIdx == 0 && to == 0 {
                            return
                        }
                        vm.moveByIdxIos(
                            fromIdx: fromIdx.toInt32(),
                            toIdx: (fromIdx > to ? to : (to - 1)).toInt32()
                        )
                    }
                }
                .onDelete { indexSet in
                    for idx in indexSet {
                        vm.deleteItem(itemDb: state.checklistItemsDb[idx])
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
