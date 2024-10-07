import SwiftUI
import shared

struct ActivityFormSheet: View {

    @EnvironmentObject private var nativeSheet: NativeSheet

    @State private var vm: ActivityFormSheetVm

    @Binding private var isPresented: Bool
    private let onSave: () -> ()

    @State private var isAddCustomHintPresented = false
    @State private var isEmojiSheetPresented = false
    @State private var isColorPickerSheetPresented = false

    @State private var sheetHeaderScroll = 0

    init(
        isPresented: Binding<Bool>,
        activity: ActivityDb?,
        onSave: @escaping () -> Void
    ) {
        self.onSave = onSave
        _isPresented = isPresented
        vm = ActivityFormSheetVm(activity: activity)
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            SheetHeaderView(
                onCancel: { isPresented.toggle() },
                title: state.headerTitle,
                doneText: state.headerDoneText,
                isDoneEnabled: state.isHeaderDoneEnabled,
                scrollToHeader: sheetHeaderScroll
            ) {
                vm.save {
                    isPresented = false
                    onSave()
                }
            }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack {

                    VStack {

                        MyListView__Padding__SectionHeader()

                        MyListView__HeaderView(title: state.inputNameHeader)

                        MyListView__Padding__HeaderSection()

                        MyListView__ItemView(
                            isFirst: true,
                            isLast: true
                        ) {

                            MyListView__ItemView__TextInputView(
                                text: state.inputNameValue,
                                placeholder: state.inputNamePlaceholder,
                                isAutofocus: false,
                                onValueChanged: { newValue in
                                    vm.setInputNameValue(text: newValue)
                                }
                            )
                        }

                        MyListView__Padding__SectionSection()

                        TextFeaturesTriggersFormView(
                            textFeatures: state.textFeatures
                        ) { textFeatures in
                            vm.setTextFeatures(newTextFeatures: textFeatures)
                        }

                        MyListView__Padding__SectionSection()
                    }

                    MyListView__ItemView(
                        isFirst: true,
                        isLast: false
                    ) {

                        MyListView__ItemView__ButtonView(
                            text: state.emojiTitle,
                            withArrow: true,
                            rightView: AnyView(

                                HStack {

                                    if let selectedEmoji = state.emoji {
                                        Text(selectedEmoji)
                                            .font(.system(size: 30))
                                            .padding(.trailing, 8)
                                    } else {
                                        Text(state.emojiNotSelected)
                                            .foregroundColor(.red)
                                            .font(.system(size: 15))
                                            .padding(.trailing, 8)
                                    }
                                }
                            )
                        ) {
                            isEmojiSheetPresented = true
                        }
                    }

                    MyListView__ItemView(
                        isFirst: false,
                        isLast: false,
                        withTopDivider: true
                    ) {

                        MyListView__ItemView__ButtonView(
                            text: state.colorTitle,
                            withArrow: false,
                            rightView: AnyView(
                                Circle()
                                    .foregroundColor(state.colorRgba.toColor())
                                    .frame(width: 32, height: 32)
                                    .padding(.trailing, 8)
                            )
                        ) {
                            isColorPickerSheetPresented = true
                        }
                    }

                    MyListView__ItemView(
                        isFirst: false,
                        isLast: false,
                        withTopDivider: true
                    ) {
                        MyListView__ItemView__SwitchView(
                            text: state.keepScreenOnTitle,
                            isActive: state.keepScreenOn
                        ) {
                            vm.toggleKeepScreenOn()
                        }
                    }

                    MyListView__ItemView(
                        isFirst: false,
                        isLast: false,
                        withTopDivider: true
                    ) {
                        MyListView__ItemView__ButtonView(
                            text: state.pomodoroTitle,
                            rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                    text: state.pomodoroNote
                                )
                            )
                        ) {
                            nativeSheet.show { isPomodoroPresented in
                                ActivityPomodoroSheet(
                                    isPresented: isPomodoroPresented,
                                    selectedTimer: state.pomodoroTimer.toInt(),
                                    onPick: { it in
                                        vm.setPomodoroTimer(pomodoroTimer: it.toInt32())
                                    }
                                )
                            }
                        }
                    }

                    VStack {

                        MyListView__ItemView(
                            isFirst: false,
                            isLast: true,
                            withTopDivider: true
                        ) {
                            
                            MyListView__ItemView__ButtonView(
                                text: state.goalsTitle,
                                withArrow: true,
                                rightView: AnyView(
                                    MyListView__ItemView__ButtonView__RightText(
                                        text: state.goalsNote,
                                        paddingEnd: 2
                                    )
                                )
                            ) {
                                nativeSheet.show { isGoalsSheetPresented in
                                    GoalPickerSheet(
                                        isPresented: isGoalsSheetPresented,
                                        onPick: { goals in
                                            // vm.setGoals(goal: goal)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    //////

                    MyListView__Padding__SectionSection()

                    MyListView__HeaderView(title: state.timerHintsHeader)

                    MyListView__Padding__HeaderSection()

                    VStack {

                        let hintsTypeName: [(title: String, type: ActivityDb__Data.TimerHintsHINT_TYPE)] = [
                            ("By History", .history),
                            ("Custom", .custom),
                        ]

                        ForEach(hintsTypeName, id: \.type) { pair in

                            let isActive = state.activityData.timer_hints.type == pair.type

                            let isFirst = hintsTypeName.first! == pair

                            MyListView__ItemView(
                                isFirst: isFirst,
                                isLast: hintsTypeName.last! == pair,
                                withTopDivider: !isFirst
                            ) {

                                VStack {

                                    MyListView__ItemView__RadioView(
                                        text: pair.title,
                                        isActive: isActive
                                    ) {
                                        withAnimation {
                                            vm.setTimerHintsType(type: pair.type)
                                        }
                                    }

                                    if isActive {

                                        if (pair.type == .custom) {

                                            VStack(alignment: .leading, spacing: 0) {

                                                // Because it can be by mistake not unique I do through count
                                                ForEach(0..<state.timerHintsCustomItems.count, id: \.self) { idx in

                                                    let customItem = state.timerHintsCustomItems[idx]

                                                    HStack(spacing: 10) {

                                                        Button(
                                                            action: {
                                                                vm.delCustomTimerHint(seconds: customItem.seconds)
                                                            },
                                                            label: {
                                                                Image(systemName: "minus.circle.fill")
                                                                    .foregroundColor(.red)
                                                            }
                                                        )

                                                        Text(customItem.text)
                                                    }
                                                    .padding(.bottom, 10)
                                                }

                                                Button(
                                                    action: {
                                                        isAddCustomHintPresented = true
                                                    },
                                                    label: {
                                                        Text("Add")
                                                    }
                                                )
                                            }
                                            .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                            .padding(.leading, H_PADDING)
                                            .padding(.bottom, 14)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if vm.activity != nil {

                    MyListView__Padding__SectionSection()

                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true
                    ) {

                        MyListView__ItemView__ActionView(
                            text: state.deleteText
                        ) {
                            vm.delete {
                                isPresented = false
                            }
                        }
                    }
                }
            }
        }
        .background(c.sheetBg)
        .sheetEnv(isPresented: $isColorPickerSheetPresented) {
            ActivityColorSheet(
                isPresented: $isColorPickerSheetPresented,
                initData: vm.buildColorPickerInitData()
            ) { colorRgba in
                vm.upColorRgba(colorRgba: colorRgba)
            }
        }
        .sheetEnv(isPresented: $isAddCustomHintPresented) {
            TimerPickerSheet(
                isPresented: $isAddCustomHintPresented,
                title: "Timer Hint",
                doneText: "Add",
                defMinutes: 30
            ) { seconds in
                vm.addCustomTimerHint(seconds: seconds.toInt32())
            }
            .presentationDetentsMediumIf16()
        }
        .sheetEnv(isPresented: $isEmojiSheetPresented) {
            SearchEmojiSheet(isPresented: $isEmojiSheetPresented) { emoji in
                vm.setEmoji(newEmoji: emoji)
            }
        }
    }
}
