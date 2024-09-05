import SwiftUI
import shared

struct GoalPickerSheet: View {

    @Binding var isPresented: Bool
    let onPick: (ActivityDb.Goal) -> Void

    ///

    @EnvironmentObject private var nativeSheet: NativeSheet
    @State private var vm = GoalPickerSheetVm()

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Sheet__HeaderView(
                    title: state.headerTitle,
                    scrollToHeader: 0,
                    bgColor: c.sheetBg
            )

            MyListView__ItemView(
                    isFirst: true,
                    isLast: true
            ) {

                MyListView__ItemView__ButtonView(
                        text: state.durationTitle,
                        withArrow: true,
                        rightView: AnyView(
                                MyListView__ItemView__ButtonView__RightText(
                                        text: state.durationNote,
                                        paddingEnd: 2
                                )
                        )
                ) {
                    nativeSheet.show { isTimerPickerPresented in
                        TimerPickerSheet(
                                isPresented: isTimerPickerPresented,
                                title: state.timerPickerSheetTitle,
                                doneText: "Done",
                                defMinutes: 60 // todo default
                        ) { seconds in
                            vm.upTime(seconds: seconds.toInt32())
                        }
                                .presentationDetentsMediumIf16()
                    }
                }
            }

            WeekDaysFormView(
                    weekDays: state.weekDays,
                    size: 36,
                    onChange: { newWeekDays in
                        vm.upWeekDays(newWeekDays: newWeekDays)
                    }
            )
                    .padding(.top, 20)
                    .padding(.leading, H_PADDING - 1)

            Spacer()

            Sheet__BottomViewDefault(
                    primaryText: state.doneTitle,
                    primaryAction: {
                        state.buildGoal { goal in
                            onPick(goal)
                            isPresented = false
                        }
                    },
                    secondaryText: "Cancel",
                    secondaryAction: {
                        isPresented = false
                    },
                    topContent: { EmptyView() },
                    startContent: { EmptyView() }
            )
        }
    }
}
