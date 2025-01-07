import SwiftUI
import shared

struct ChecklistSheet: View {
    
    @State private var vm: ChecklistSheetVm
    
    @Binding private var isPresented: Bool
    
    init(
        isPresented: Binding<Bool>,
        checklist: ChecklistDb
    ) {
        _isPresented = isPresented
        _vm = State(initialValue: ChecklistSheetVm(checklistDb: checklist))
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
                    maxLines: 9,
                    onDelete: {
                        isPresented = false
                    }
                )
            }
        }
        .background(c.sheetBg)
    }
}
