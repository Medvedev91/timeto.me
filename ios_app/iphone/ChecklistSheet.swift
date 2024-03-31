import SwiftUI
import shared

struct ChecklistSheet: View {
    
    @State private var vm: ChecklistSheetVM
    
    @Binding private var isPresented: Bool
    
    init(
        isPresented: Binding<Bool>,
        checklist: ChecklistDb
    ) {
        _isPresented = isPresented
        _vm = State(initialValue: ChecklistSheetVM(checklistDb: checklist))
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .ZStack(alignment: .top)) { state in
            
            VStack {
                
                Text(state.checklistDb.name)
                    .font(.system(size: 28, weight: .bold))
                    .padding(.top, 20)
                    .padding(.bottom, 15)
                
                ChecklistView(
                    checklistDb: state.checklistDb,
                    onDelete: {
                        // todo
                    },
                    bottomPadding: 0
                )
            }
        }
        .background(c.sheetBg)
    }
}
