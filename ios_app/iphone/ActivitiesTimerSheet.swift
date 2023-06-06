import SwiftUI
import shared

extension TimetoSheet {

    func showActivitiesTimerSheet(
            isPresented: Binding<Bool>,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            onStart: @escaping () -> Void
    ) {
        items.append(
                TimetoSheet__Item(
                        isPresented: isPresented,
                        content: {
                            AnyView(
                                    ActivitiesTimerSheet(
                                            isPresented: isPresented,
                                            timerContext: timerContext
                                    ) {
                                        isPresented.wrappedValue = false
                                    }
                                            .cornerRadius(10, onTop: true, onBottom: false)
                            )
                        }
                )
        )
    }
}

struct ActivitiesTimerSheet: View {

    @State private var vm: ActivitiesTimerSheetVM

    @State private var sheetActivity: ActivityModel? = nil
    @Binding private var isPresented: Bool

    private let timerContext: ActivityTimerSheetVM.TimerContext?
    private let onStart: () -> Void

    init(
            isPresented: Binding<Bool>,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            onStart: @escaping () -> Void
    ) {
        _isPresented = isPresented
        self.timerContext = timerContext
        self.onStart = onStart

        _vm = State(initialValue: ActivitiesTimerSheetVM(timerContext: timerContext))
    }

    var body: some View {

        // todo If inside VMView twitch on open sheet
        let sheetHeight = max(
            /// If height too small - invalid UI
                ActivityTimerSheet.RECOMMENDED_HEIGHT,
            /// Do not be afraid of too much height because the native sheet will cut
                40.0 + DI.activitiesSorted.count.toDouble() * TasksView__TaskRowView__ActivityRowView__ButtonStyle.LIST_ITEM_HEIGHT
        )

        ScrollView {

            VMView(vm: vm) { state in

                ZStack {

                    if let sheetActivity = sheetActivity {
                        ActivityTimerSheet(
                                activity: sheetActivity,
                                isPresented: $isPresented,
                                timerContext: timerContext,
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
        }
                .frame(maxHeight: sheetHeight)
                .background(Color(.mySecondaryBackground))
                .listStyle(.plain)
                .listSectionSeparatorTint(.clear)
                .onDisappear {
                    sheetActivity = nil
                }
    }
}
