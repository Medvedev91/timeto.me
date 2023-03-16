import SwiftUI
import shared

struct TextFeaturesTimerFormView: View {

    @State private var vm: TextFeaturesFormVM

    private let onChange: (TextFeatures) -> Void

    @State private var isActivitySheetPresented = false
    @State private var isTimerSheetPresented = false


    private let _textFeaturesState: TextFeatures

    init(
            textFeatures: TextFeatures,
            onChange: @escaping (TextFeatures) -> Void
    ) {
        _textFeaturesState = textFeatures
        self.onChange = onChange
        _vm = State(initialValue: TextFeaturesFormVM(initTextFeatures: textFeatures))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            EmptyView()
                    .onChange(of: _textFeaturesState) { newTextFeatures in
                        vm = TextFeaturesFormVM(initTextFeatures: newTextFeatures)
                    }

            MyListView__ItemView(
                    isFirst: true,
                    isLast: false
            ) {

                MyListView__ItemView__ButtonView(
                        text: state.activityTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: state.activityNote,
                                        paddingEnd: 2,
                                        textColor: state.activityColorOrNull?.toColor()
                                )
                        )
                ) {
                    hideKeyboard()
                    isActivitySheetPresented = true
                }
                        .sheetEnv(isPresented: $isActivitySheetPresented) {
                            ActivityPickerSheet(
                                    isPresented: $isActivitySheetPresented
                            ) { activity in
                                vm.upActivity(activity: activity)
                            }
                        }
            }

            MyListView__ItemView(
                    isFirst: false,
                    isLast: true,
                    withTopDivider: true
            ) {

                MyListView__ItemView__ButtonView(
                        text: state.timerTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: state.timerNote,
                                        paddingEnd: 2,
                                        textColor: state.timerColorOrNull?.toColor()
                                )
                        )
                ) {
                    hideKeyboard()
                    isTimerSheetPresented = true
                }
                        .sheetEnv(isPresented: $isTimerSheetPresented) {
                            TimerPickerSheet(
                                    isPresented: $isTimerSheetPresented,
                                    title: "Timer",
                                    doneText: "Done",
                                    defMinutes: 30
                            ) { seconds in
                                vm.upTimer(seconds: seconds.toInt32())
                            }
                                    .presentationDetentsMediumIf16()
                        }
            }
        }
    }
}
