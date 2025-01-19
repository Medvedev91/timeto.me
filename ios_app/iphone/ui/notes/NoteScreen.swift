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
                state: state
            )
        }
    }
}

private struct NoteScreenInner: View {
    
    let vm: NoteVm
    let state: NoteVm.State
    
    ///
    
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
    }
}
