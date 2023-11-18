import SwiftUI
import shared

struct TextFeaturesTimerFormView: View {

    private let formUI: TextFeaturesTimerFormUI
    private let onChange: (TextFeatures) -> Void

    @State private var isActivitySheetPresented = false
    @State private var isTimerSheetPresented = false

    init(
            textFeatures: TextFeatures,
            onChange: @escaping (TextFeatures) -> Void
    ) {
        self.onChange = onChange
        formUI = TextFeaturesTimerFormUI(textFeatures: textFeatures)
    }

    var body: some View {

        VStack {

            MyListView__ItemView(
                    isFirst: true,
                    isLast: false
            ) {

                MyListView__ItemView__ButtonView(
                        text: formUI.activityTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: formUI.activityNote,
                                        paddingEnd: 2,
                                        textColor: formUI.activityColorOrNull?.toColor()
                                )
                        )
                ) {
                    isActivitySheetPresented = true
                }
                        .sheetEnv(isPresented: $isActivitySheetPresented) {
                            ActivityPickerSheet(
                                    isPresented: $isActivitySheetPresented
                            ) { activity in
                                onChange(formUI.setActivity(activity: activity))
                            }
                        }
            }

            MyListView__ItemView(
                    isFirst: false,
                    isLast: true,
                    withTopDivider: true
            ) {

                MyListView__ItemView__ButtonView(
                        text: formUI.timerTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: formUI.timerNote,
                                        paddingEnd: 2,
                                        textColor: formUI.timerColorOrNull?.toColor()
                                )
                        )
                ) {
                    isTimerSheetPresented = true
                }
                        .sheetEnv(isPresented: $isTimerSheetPresented) {
                            TimerPickerSheet(
                                    isPresented: $isTimerSheetPresented,
                                    title: "Timer",
                                    doneText: "Done",
                                    defMinutes: 30
                            ) { seconds in
                                onChange(formUI.setTimer(seconds: seconds.toInt32()))
                            }
                                    .presentationDetentsMediumIf16()
                        }
            }
        }
    }
}
