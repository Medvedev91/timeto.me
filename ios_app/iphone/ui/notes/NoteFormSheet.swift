import SwiftUI
import shared

struct NoteFormSheet: View {
    
    let noteDb: NoteDb?
    let onDelete: () -> Void
    
    var body: some View {
        
        VmView({
            NoteFormSheetVm(
                noteDb: noteDb
            )
        }) { vm, state in
            
            NoteFormSheetInner(
                vm: vm,
                state: state,
                text: state.text,
                onDelete: onDelete
            )
        }
    }
}

private struct NoteFormSheetInner: View {
    
    let vm: NoteFormSheetVm
    let state: NoteFormSheetVm.State
    
    @State var text: String
    
    let onDelete: () -> Void
    
    ///
    
    @FocusState private var isFocused: Bool
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List {
            
            TextField(
                state.textPlaceholder,
                text: $text,
                axis: .vertical
            )
            .focused($isFocused)
            .onChange(of: text) { _, new in
                vm.setText(text: new)
            }
            
            if let noteDb = state.noteDb {
                Section {
                    Button("Delete Note") {
                        vm.delete(
                            noteDb: noteDb,
                            dialogsManager: navigation,
                            onDelete: {
                                dismiss()
                                onDelete()
                            }
                        )
                    }
                    .foregroundColor(.red)
                }
            }
        }
        .contentMargins(.top, 14)
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
                Button(state.saveText) {
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: {
                            dismiss()
                        }
                    )
                }
                .fontWeight(.bold)
                .disabled(!state.isSaveEnabled)
            }
        }
        .onAppear {
            isFocused = true
        }
    }
}
