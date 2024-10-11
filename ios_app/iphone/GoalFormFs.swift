import SwiftUI
import shared

struct GoalFormFs: View {
    
    @State private var vm: GoalFormVm
    @Binding private var isPresented: Bool
    private let onSelect: (ActivityFormSheetVm.GoalFormUi) -> ()
    
    @State private var fsHeaderScroll = 0
    
    @EnvironmentObject private var nativeSheet: NativeSheet

    init(
        isPresented: Binding<Bool>,
        initGoalFormUi: ActivityFormSheetVm.GoalFormUi?,
        onSelect: @escaping (ActivityFormSheetVm.GoalFormUi) -> ()
    ) {
        _isPresented = isPresented
        self.onSelect = onSelect
        vm = GoalFormVm(initGoalFormUi: initGoalFormUi)
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack()) { state in
            
            Fs__HeaderAction(
                title: state.headerTitle,
                actionText: state.headerDoneText,
                scrollToHeader: fsHeaderScroll,
                onCancel: {
                    isPresented = false
                },
                onDone: {
                    vm.buildFormUi { formUi in
                        onSelect(formUi)
                        isPresented = false
                    }
                }
            )
            
            ScrollViewWithVListener(showsIndicators: false, vScroll: $fsHeaderScroll) {
                
                VStack {
                    
                    MyListView__PaddingFirst()
                    
                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true
                    ) {
                        
                        MyListView__ItemView__TextInputView(
                            text: state.note,
                            placeholder: state.notePlaceholder,
                            isAutofocus: false
                        ) { newValue in
                            vm.setNote(note: newValue)
                        }
                    }
                    
                    MyListView__Padding__SectionSection()
                    
                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true
                    ) {

                        MyListView__Item__Button(
                            text: state.finishedTitle,
                            rightView: {
                                MyListView__Item__Button__RightText(
                                    text: state.finishedText,
                                    color: .white
                                )
                            },
                            onClick: {
                                nativeSheet.show { isEmojiSheetPresented in
                                    SearchEmojiSheet(
                                        isPresented: isEmojiSheetPresented
                                    ) { emoji in
                                        vm.setFinishedText(text: emoji)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
