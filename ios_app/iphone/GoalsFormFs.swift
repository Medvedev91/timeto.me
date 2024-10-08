import SwiftUI
import shared

struct GoalsFormFs: View {
    
    @State private var vm: GoalsFormVm
    @Binding private var isPresented: Bool
    private let onSelected: ([ActivityFormSheetVm.GoalFormUi]) -> ()
    
    @State private var sheetHeaderScroll = 0

    init(
        isPresented: Binding<Bool>,
        initGoalFormsUi: [ActivityFormSheetVm.GoalFormUi],
        onSelected: @escaping ([ActivityFormSheetVm.GoalFormUi]) -> ()
    ) {
        _isPresented = isPresented
        self.onSelected = onSelected
        vm = GoalsFormVm(initGoalFormsUi: initGoalFormsUi)
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack()) { state in
            
            Fs__HeaderAction(
                title: state.headerTitle,
                actionText: state.headerDoneText,
                scrollToHeader: sheetHeaderScroll,
                onCancel: {
                    isPresented = false
                },
                onDone: {
                    onSelected(state.goalFormsUi)
                    isPresented = false
                }
            )
        }
    }
}
