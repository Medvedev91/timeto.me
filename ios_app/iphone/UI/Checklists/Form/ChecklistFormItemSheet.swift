import SwiftUI
import shared

struct ChecklistFormItemSheet: View {
    
    let checklistDb: ChecklistDb
    let checklistItemDb: ChecklistItemDb?
    
    var body: some View {
        VmView({
            ChecklistFormItemVm(
                checklistDb: checklistDb,
                checklistItemDb: checklistItemDb
            )
        }) { vm, state in
            ChecklistFormItemSheetInner(
                vm: vm,
                state: state,
                text: state.text
            )
        }
    }
}

///

private struct ChecklistFormItemSheetInner: View {
    
    let vm: ChecklistFormItemVm
    let state: ChecklistFormItemVm.State
    
    @State var text: String
    
    ///
    
    @FocusState private var isFocused: Bool
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List {
            
            TextField(
                text: $text
            ) {
            }
            .focused($isFocused)
            .onChange(of: text) { _, new in
                vm.setText(text: new)
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text("Checklists")
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.checklistsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ChecklistsPickerSheet(
                            initChecklistsDb: state.nestedChecklistsDb,
                            onDone: { newChecklistsDb in
                                vm.setNestedChecklists(newNestedChecklistsDb: newChecklistsDb)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text("Shortcuts")
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.shortcutsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ShortcutsPickerSheet(
                            initShortcutsDb: state.nestedShortcutsDb,
                            onDone: { newShortcutsDb in
                                vm.setNestedShortcuts(newNestedShortcuts: newShortcutsDb)
                            }
                        )
                    }
                )
            }
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.title)
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button(state.saveButtonText) {
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: {
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
                .disabled(!state.isSaveEnabled)
            }
        }
        .onAppear {
            isFocused = true
        }
    }
}
