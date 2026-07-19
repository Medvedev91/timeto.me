import SwiftUI
import shared

struct NotesView: View {
    
    let noteFolderDb: NoteFolderDb
    let hPadding: CGFloat
    let withDivider: Bool
    
    var body: some View {
        VmView({
            NotesVm(
                noteFolderDb: noteFolderDb,
            )
        }) { vm, state in
            let state = vm.state.value as! NotesVm.State
            NotesViewLocal(
                state: state,
                notesUi: state.notesUi,
                hPadding: hPadding,
                withDivider: withDivider,
            )
        }
    }
}

private struct NotesViewLocal: View {
    
    let state: NotesVm.State
    let notesUi: [NotesVm.NoteUi]
    let hPadding: CGFloat
    let withDivider: Bool

    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack {
                ForEach(notesUi, id: \.noteDb.id) { noteUi in
                    Button(
                        action: {
                            navigation.push(.note(noteDb: noteUi.noteDb, onDelete: {}))
                        },
                        label: {
                            ZStack {
                                if withDivider && (notesUi.first != noteUi) {
                                    VStack {
                                        Divider()
                                            .padding(.leading, hPadding)
                                        Spacer()
                                    }
                                }
                                
                                Text(noteUi.text)
                                    .foregroundColor(.white)
                                    .lineLimit(3)
                                    .padding(.horizontal, hPadding)
                                    .padding(.vertical, 4)
                                    .textAlign(.leading)
                            }
                            .frame(minHeight: HomeScreen__itemHeight)
                        },
                    )
                }
            }
        }
        .defaultScrollAnchor(.bottom)
    }
}
