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

    @EnvironmentObject private var nativeSheet: NativeSheet

    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        VStack {
            
            ScrollView {
                
                Padding(vertical: 8)
                
                ForEach(state.checklistItemsUi, id: \.checklistItemDb.id) { checklistItemUi in
                    
                    VStack {
                        
                        if !checklistItemUi.isFirst {
                            DividerFg()
                                .padding(.leading, 27)
                        }
                        
                        Spacer()
                        
                        HStack(spacing: 8) {
                            
                            Button(
                                action: {
                                    vm.deleteItem(itemDb: checklistItemUi.checklistItemDb)
                                },
                                label: {
                                    Image(systemName: "minus.circle.fill")
                                        .font(.system(size: 16))
                                        .foregroundColor(.red)
                                }
                            )
                            .buttonStyle(.plain)
                            
                            Text(checklistItemUi.checklistItemDb.text)
                                .lineLimit(1)
                            
                            Spacer()
                            
                            Button(
                                action: {
                                    nativeSheet.show { isPresented in
                                        ChecklistItemFormSheet(
                                            isPresented: isPresented,
                                            checklist: state.checklistDb,
                                            checklistItem: checklistItemUi.checklistItemDb
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
                        .padding(.vertical, 12)
                        
                        Spacer()
                    }
                    .padding(.horizontal, H_PADDING)
                }
                
                HStack {
                    
                    Button(
                        action: {
                            nativeSheet.show { isPresented in
                                ChecklistItemFormSheet(
                                    isPresented: isPresented,
                                    checklist: state.checklistDb,
                                    checklistItem: nil
                                )
                            }
                        },
                        label: {
                            Text(state.newItemButton)
                                .foregroundColor(c.blue)
                                .padding(.leading, H_PADDING)
                        }
                    )
                    
                    Spacer()
                }
                .padding(.top, 12)
            }
        }
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
        }
    }
}
