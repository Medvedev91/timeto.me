import SwiftUI
import shared

struct NoteScreen: View {
    
    let noteDb: NoteDb
    let onDelete: () -> Void
    
    var body: some View {
        
        VmView({
            NoteVm(
                noteDb: noteDb
            )
        }) { vm, state in
            
            NoteScreenInner(
                vm: vm,
                state: state,
                onDelete: onDelete
            )
        }
    }
}

private struct NoteScreenInner: View {
    
    let vm: NoteVm
    let state: NoteVm.State
    
    let onDelete: () -> Void
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        ScrollView {
            
            Text(state.noteDb.text)
                .textAlign(.leading)
                .padding(.horizontal, H_PADDING)
                .padding(.top, 8)
                .padding(.bottom, 16)
        }
        .contentMarginsTabBar()
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button("Edit") {
                    navigation.sheet {
                        NoteFormSheet(
                            noteDb: state.noteDb,
                            onDelete: onDelete
                        )
                    }
                }
            }
        }
    }
}
