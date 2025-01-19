import SwiftUI
import shared

struct TaskFoldersFormSheet: View {
    
    var body: some View {
        VmView({
            FoldersSettingsVm()
        }) { vm, state in
            TaskFoldersFormSheetInner(
                vm: vm,
                state: state
            )
        }
    }
}

private struct TaskFoldersFormSheetInner: View {
    
    let vm: FoldersSettingsVm
    let state: FoldersSettingsVm.State
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @State private var editMode: EditMode = .active
    
    var body: some View {
        
        List {
            
            ForEach(state.foldersDb, id: \.id) { folderDb in
                Button(folderDb.name) {
                }
                .foregroundColor(.primary)
            }
            .onMoveVm { from, to in
                vm.moveIos(from: from, to: to)
            }
            .onDeleteVm { idx in
                vm.delete(
                    folderDb: state.foldersDb[idx],
                    dialogsManager: navigation
                )
            }
            
            Section {
                
                Button("New Folder") {
                }
            }
            
            if let tmrwButtonUi = state.tmrwButtonUi {
                Section {
                    Button(tmrwButtonUi.text) {
                        tmrwButtonUi.add(
                            dialogsManager: navigation
                        )
                    }
                }
            }
        }
        .environment(\.editMode, $editMode)
        .contentMargins(.top, 14)
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.title)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    dismiss()
                }
                .fontWeight(.bold)
            }
        }
    }
}
