import SwiftUI
import shared

struct NotesScreen: View {
    
    let noteFolderDb: NoteFolderDb
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        VmView({
            NotesScreenVm(
                initNoteFolderDb: noteFolderDb,
            )
        }) { vm, _ in
            let state = vm.state.value as! NotesScreenVm.State
            NotesView(
                noteFolderDb: noteFolderDb,
                hPadding: H_PADDING,
                withDivider: true,
            )
            .contentMarginsTabBar(extra: 12)
            .navigationTitle(state.title)
            .toolbarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItemGroup(placement: .primaryAction) {
                    
                    Button("Edit") {
                        navigation.sheet {
                            NoteFolderFormSheet(
                                noteFolderDb: noteFolderDb,
                                onDelete: {
                                    dismiss()
                                },
                            )
                        }
                    }
                    
                    Button {
                        navigation.sheet {
                            NoteFormSheet(
                                noteFormLogic: NoteFormLogic.NewNote(
                                    noteFolderDb: noteFolderDb,
                                ),
                                onDelete: {},
                            )
                        }
                    } label: {
                        Image(systemName: "plus")
                    }
                    .tint(.blue)
                }
            }
        }
    }
}
