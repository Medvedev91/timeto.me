import SwiftUI
import shared

struct TaskFoldersFormSheet: View {
    
    var body: some View {
        VmView({
            FoldersSettingsVm()
        }) { vm, state in
            TaskFoldersFormSheetInner(
                vm: vm,
                state: state,
                foldersDbAnimate: state.foldersDb
            )
        }
    }
}

private struct TaskFoldersFormSheetInner: View {
    
    let vm: FoldersSettingsVm
    let state: FoldersSettingsVm.State
    
    @State var foldersDbAnimate: [TaskFolderDb]
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @State private var editMode: EditMode = .active
    
    @State private var withFoldersAnimation = true
    
    ///
    
    var body: some View {
        
        List {
            
            ForEach(foldersDbAnimate, id: \.id) { folderDb in
                Button(folderDb.name) {
                }
                .foregroundColor(.primary)
            }
            .onMoveVm { from, to in
                withFoldersAnimation = false
                vm.moveIos(from: from, to: to)
            }
            .onDeleteVm { idx in
                vm.delete(
                    folderDb: foldersDbAnimate[idx],
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
        .animateVmValue(
            value: state.foldersDb,
            state: $foldersDbAnimate,
            enabled: withFoldersAnimation,
            onChange: {
                withFoldersAnimation = true
            }
        )
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
