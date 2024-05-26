import SwiftUI
import shared

struct RepeatingsFormSheet: View {

    @State private var vm: RepeatingFormSheetVM
    @Binding private var isPresented: Bool
    private let onSave: () -> ()

    @State private var isDaytimeSheetPresented = false
    @State private var sheetHeaderScroll = 0
    @State private var isMoreSettingsVisible = false

    @EnvironmentObject private var fs: Fs

    init(
        isPresented: Binding<Bool>,
        editedRepeating: RepeatingDb?,
        onSave: @escaping () -> ()
    ) {
        _isPresented = isPresented
        self.onSave = onSave
        vm = RepeatingFormSheetVM(repeating: editedRepeating)
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Fs__HeaderAction(
                title: state.headerTitle,
                actionText: state.headerDoneText,
                scrollToHeader: sheetHeaderScroll,
                onCancel: {
                    isPresented.toggle()
                },
                onDone: {
                    vm.save {
                        onSave()
                        isPresented = false
                    }
                }
            )

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack {

                    VStack {

                        MyListView__PaddingFirst()

                        MyListView__ItemView(
                            isFirst: true,
                            isLast: true,
                            bgColor: c.fg
                        ) {

                            MyListView__ItemView__TextInputView(
                                text: state.inputTextValue,
                                placeholder: "Task",
                                isAutofocus: false
                            ) { newValue in
                                vm.setTextValue(text: newValue)
                            }
                        }

                        MyListView__Padding__SectionSection()

                        MyListView__ItemView(
                            isFirst: true,
                            isLast: false,
                            bgColor: c.fg
                        ) {

                            MyListView__ItemView__ButtonView(
                                text: state.periodTitle,
                                withArrow: true,
                                rightView: AnyView(
                                    MyListView__ItemView__ButtonView__RightText(
                                        text: state.periodNote,
                                        paddingEnd: 2,
                                        textColor: state.periodNoteColor?.toColor()
                                    )
                                )
                            ) {
                                fs.show { isPeriodPresented in
                                    RepeatingFormPeriodFs(
                                        isPresented: isPeriodPresented,
                                        defaultPeriod: state.period
                                    ) { period in
                                        vm.setPeriod(period: period)
                                    }
                                }
                            }
                        }

                        MyListView__ItemView(
                            isFirst: false,
                            isLast: true,
                            bgColor: c.fg,
                            withTopDivider: true
                        ) {

                            MyListView__ItemView__ButtonView(
                                text: state.daytimeHeader,
                                withArrow: true,
                                rightView: AnyView(
                                    MyListView__ItemView__ButtonView__RightText(
                                        text: state.daytimeNote,
                                        paddingEnd: 2
                                    )
                                )
                            ) {
                                isDaytimeSheetPresented = true
                            }
                            .sheetEnv(isPresented: $isDaytimeSheetPresented) {
                                DaytimePickerSheet(
                                    isPresented: $isDaytimeSheetPresented,
                                    title: state.daytimeHeader,
                                    doneText: "Done",
                                    daytimeModel: state.defDaytimeModel,
                                    onPick: { daytimeModel in
                                        vm.upDaytime(daytimeModel: daytimeModel)
                                    },
                                    onRemove: {
                                        vm.upDaytime(daytimeModel: nil)
                                    }

                                )
                                .presentationDetentsMediumIf16()
                            }
                        }

                        MyListView__Padding__SectionSection()

                        TextFeaturesTimerFormView(
                            textFeatures: state.textFeatures,
                            bgColor: c.fg
                        ) { textFeatures in
                            vm.upTextFeatures(textFeatures: textFeatures)
                        }

                        if !isMoreSettingsVisible {

                            HStack {

                                Button(
                                    action: {
                                        withAnimation {
                                            isMoreSettingsVisible = true
                                        }
                                    },
                                    label: {
                                        Text(state.moreSettingText)
                                            .font(.system(size: 16))
                                    }
                                )
                                .padding(.top, 26)
                                .padding(.leading, H_PADDING)

                                Spacer()
                            }
                        }

                        if isMoreSettingsVisible {

                            MyListView__Padding__SectionSection()

                            TextFeaturesTriggersFormView(
                                textFeatures: state.textFeatures,
                                bgColor: c.fg
                            ) { textFeatures in
                                vm.upTextFeatures(textFeatures: textFeatures)
                            }
                            MyListView__Padding__SectionSection()

                            MyListView__ItemView(
                                isFirst: true,
                                isLast: true,
                                bgColor: c.fg
                            ) {
                                MyListView__ItemView__SwitchView(
                                    text: state.isImportantHeader,
                                    isActive: state.isImportant
                                ) {
                                    vm.toggleIsImportant()
                                }
                            }
                        }
                    }

                    Spacer()
                        .frame(minHeight: 20)
                }
            }
        }
        .background(c.bg)
    }
}

