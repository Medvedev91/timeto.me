import SwiftUI
import shared

struct GoalsFormFs: View {
    
    @State private var vm: GoalsFormVm
    @Binding private var isPresented: Bool
    private let onSelected: ([GoalFormUi]) -> ()
    
    @State private var sheetHeaderScroll = 0
    
    @EnvironmentObject private var fs: Fs

    init(
        isPresented: Binding<Bool>,
        initGoalFormsUi: [GoalFormUi],
        onSelected: @escaping ([GoalFormUi]) -> ()
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
            
            Spacer()
            
            Fs__BottomBar {
                
                HStack {
                    
                    Fs__BottomBar__PlusButton(
                        text: state.newGoalButtonText,
                        onClick: {
                            fs.show { fsGoalFormLayer in
                                GoalFormFs(
                                    isPresented: fsGoalFormLayer,
                                    initGoalFormUi: nil,
                                    onSelect: { newGoalFormUi in
                                    }
                                )
                            }
                        }
                    )
                    .padding(.leading, H_PADDING_HALF)
                    .padding(.vertical, 8)
                    
                    Spacer()
                }
            }
        }
    }
}
