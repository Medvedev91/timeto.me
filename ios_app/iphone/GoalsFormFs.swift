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
            
            ScrollViewWithVListener(
                showsIndicators: false,
                vScroll: $sheetHeaderScroll
            ) {
                
                VStack {
                    
                    MyListView__PaddingFirst()
                    
                    let goalFormsUi = state.goalFormsUi
                    ForEachIndexed(goalFormsUi) { idx, formUi in
                        
                        MyListView__ItemView(
                            isFirst: idx == 0,
                            isLast: goalFormsUi.count - 1 == idx,
                            bgColor: c.fg,
                            withTopDivider: idx > 0
                        ) {
                            
                            MyListView__Item__Button(
                                text: formUi.period.note(),
                                rightView: {
                                    MyListView__Item__Button__RightText(
                                        text: formUi.durationString
                                    )
                                }
                            ) {
                                fs.show { ip in
                                    GoalFormFs(
                                        isPresented: ip,
                                        initGoalFormUi: formUi,
                                        onSelect: { newFormUi in
                                            vm.upGoalFormUi(idx: idx.toInt32(), goalFormUi: newFormUi)
                                        },
                                        onDelete: {
                                            vm.deleteGoalFormUi(idx: idx.toInt32())
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
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
                                        vm.addGoalFormUi(goalFormUi: newGoalFormUi)
                                    },
                                    onDelete: nil
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
