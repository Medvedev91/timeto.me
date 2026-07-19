import SwiftUI
import shared

struct HomeNotesView: View {
    
    let noteFolderDb: NoteFolderDb
    
    var body: some View {
        
        Text(noteFolderDb.name)
            .font(.system(size: 30, weight: .bold))
            .foregroundColor(.primary)
            .padding(.leading, HomeScreen__hPadding)
            .padding(.bottom, 8)
            .textAlign(.leading)
        
        NotesView(
            noteFolderDb: noteFolderDb,
            hPadding: HomeScreen__hPadding,
            withDivider: false,
        )
    }
}
