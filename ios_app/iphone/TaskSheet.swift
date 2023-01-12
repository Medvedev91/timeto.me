import SwiftUI
import shared

struct TaskSheet: View {

    @State private var vm: TaskSheetVM

    @State private var sheetActivity: ActivityModel? = nil
    @Binding private var isPresented: Bool

    private let task: TaskModel
    private let onStart: () -> Void

    init(
            isPresented: Binding<Bool>,
            task: TaskModel,
            onStart: @escaping () -> Void
    ) {
        _isPresented = isPresented
        self.task = task
        self.onStart = onStart

        _vm = State(initialValue: TaskSheetVM(task: task))
    }

    var body: some View {

        // todo If inside VMView twitch on open sheet
        let sheetHeight = max(
            /// If height too small - invalid UI
                ActivityTimerSheet.RECOMMENDED_HEIGHT,
            /// Do not be afraid of too much height because the native sheet will cut
                40.0 + DI.activitiesSorted.count.toDouble() * TasksView__TaskRowView__ActivityRowView__ButtonStyle.LIST_ITEM_HEIGHT
        )

        VMView(vm: vm) { state in

            ZStack {

                if let sheetActivity = sheetActivity {
                    ActivityTimerSheet(
                            activity: sheetActivity,
                            isPresented: $isPresented,
                            timerContext: ActivityTimerSheetVM.TimerContextTask(task: task),
                            onStart: {
                                onStart()
                            }
                    )
                } else {

                    ScrollView {

                        VStack(spacing: 0) {

                            ZStack {
                            }
                                    .frame(height: 10)

                            ForEach(state.allActivities, id: \.activity.id) { activityUI in

                                TasksView__TaskRowView__ActivityRowView(
                                        activityUI: activityUI,
                                        historySeconds: activityUI.historySeconds.map { $0.toInt() },
                                        onClickOnTimer: {
                                            sheetActivity = activityUI.activity
                                        },
                                        onStarted: {
                                            onStart()
                                        }
                                )
                            }
                                    .buttonStyle(TasksView__TaskRowView__ActivityRowView__ButtonStyle())

                            ZStack {
                            }
                                    .frame(height: 30)
                        }
                    }
                }
            }
        }
                .presentationDetentsHeightIf16(sheetHeight, withDragIndicator: true)
                .background(Color(.mySecondaryBackground))
                .listStyle(.plain)
                .listSectionSeparatorTint(.clear)
                .onDisappear {
                    sheetActivity = nil
                }
    }
}
