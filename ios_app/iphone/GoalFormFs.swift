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
                        isLast: true,
                        bgColor: c.fg
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
                        isLast: false,
                        bgColor: c.fg
                    ) {
                        MyListView__Item__Button(
                            text: state.periodTitle,
                            rightView: {
                                MyListView__Item__Button__RightText(
                                    text: state.periodNote,
                                    color: state.periodNoteColor?.toColor()
                                )
                            }
                        ) {
                        }
                    }

                    MyListView__ItemView(
                        isFirst: false,
                        isLast: true,
                        bgColor: c.fg,
                        withTopDivider: true
                    ) {
                        MyListView__Item__Button(
                            text: state.durationTitle,
                            rightView: {
                                MyListView__Item__Button__RightText(
                                    text: state.durationNote
                                )
                            }
                        ) {
                            nativeSheet.show { isTimerPickerPresented in
                                TimerPickerSheet(
                                    isPresented: isTimerPickerPresented,
                                    title: state.durationPickerSheetTitle,
                                    doneText: "Done",
                                    defMinutes: state.durationDefMinutes.toInt(),
                                    onDone: { seconds in
                                        vm.setDuration(seconds: seconds.toInt32())
                                    }
                                )
                                .presentationDetentsMediumIf16()
                            }
                        }
                    }
                    
                    MyListView__Padding__SectionSection()

                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true,
                        bgColor: c.fg
                    ) {

                        MyListView__Item__Button(
                            text: state.timerTitle,
                            rightView: {
                                MyListView__Item__Button__RightText(
                                    text: state.timerNote,
                                    color: state.timerNoteColor?.toColor()
                                )
                            }
                        ) {
                            nativeSheet.show { isTimerPickerPresented in
                                TimerPickerSheet(
                                    isPresented: isTimerPickerPresented,
                                    title: state.timerPickerSheetTitle,
                                    doneText: "Done",
                                    defMinutes: state.timerDefaultMinutes.toInt()
                                ) { seconds in
                                    vm.setTimer(timer: seconds.toInt32())
                                }
                                .presentationDetentsMediumIf16()
                            }
                        }
                    }
                    
                    MyListView__Padding__SectionSection()
                    
                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true,
                        bgColor: c.fg
                    ) {

                        MyListView__Item__Button(
                            text: state.finishedTitle,
                            rightView: {
                                MyListView__Item__Button__RightText(
                                    text: state.finishedText,
                                    color: .white,
                                    fontSize: 26
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
                    
                    MyListView__Padding__SectionSection()

                    TextFeaturesTriggersFormView(
                        textFeatures: state.textFeatures,
                        bgColor: c.fg
                    ) { newTf in
                        vm.setTextFeatures(tf: newTf)
                    }
                }
            }
        }
    }
}
