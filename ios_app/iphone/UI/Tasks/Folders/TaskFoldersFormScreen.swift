import SwiftUI
import shared

struct TaskFoldersFormScreen: View {
    
    var body: some View {
        VmView({
            TaskFoldersFormVm()
        }) { vm, state in
            TaskFoldersFormScreenInner(
                vm: vm,
                state: state,
                foldersDbAnimate: state.foldersDb,
                tmrwButtonUiAnimation: state.tmrwButtonUi
            )
        }
    }
}

private struct TaskFoldersFormScreenInner: View {
    
    let vm: TaskFoldersFormVm
    let state: TaskFoldersFormVm.State
    
    @State var foldersDbAnimate: [TaskFolderDb]
    @State var tmrwButtonUiAnimation: TaskFoldersFormVm.TmrwButtonUi?
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    @State private var editMode: EditMode = .active
    
    @State private var withFoldersAnimation = true
    
    ///
    
    var body: some View {
        
        List {
            
            ForEach(foldersDbAnimate, id: \.id) { folderDb in
                Button(folderDb.name) {
                    navigation.sheet {
                        TaskFolderFormSheet(
                            taskFolderDb: folderDb
                        )
                    }
                }
                .foregroundColor(.primary)
                .contextMenu {
                    Button(
                        action: {
                            navigation.sheet {
                                TaskFolderFormSheet(
                                    taskFolderDb: folderDb
                                )
                            }
                        },
                        label: {
                            Label("Edit", systemImage: "square.and.pencil")
                        }
                    )
                }
            }
            .onMoveVm { fromIdx, toIdx in
                withFoldersAnimation = false
                vm.moveIos(fromIdx: fromIdx, toIdx: toIdx)
            }
            
            Section {
                
                Button("New Folder") {
                    navigation.sheet {
                        TaskFolderFormSheet(
                            taskFolderDb: nil
                        )
                    }
                }
            }
            
            if let tmrwButtonUi = tmrwButtonUiAnimation {
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
            vmValue: state.foldersDb,
            swiftState: $foldersDbAnimate,
            enabled: withFoldersAnimation,
            onChange: {
                withFoldersAnimation = true
            }
        )
        .animateVmValue(
            vmValue: state.tmrwButtonUi,
            swiftState: $tmrwButtonUiAnimation
        )
        .environment(\.editMode, $editMode)
        .myFormContentMargins()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.title)
    }
}
