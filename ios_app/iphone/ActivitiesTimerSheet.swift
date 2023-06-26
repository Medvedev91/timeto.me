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
                                        onStart()
                                    }
                                            .cornerRadius(10, onTop: true, onBottom: false)
                            )
                        }
                )
        )
    }
}

private let bgColor = Color(.bgSheet)
private let itemHeight = 46.0
private let topPadding = 2.0
private let bottomPadding = 30.0

private struct ActivitiesTimerSheet: View {

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
                bottomPadding * topPadding + (DI.activitiesSorted.count.toDouble() * itemHeight)
        )

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

                        VStack {

                            Padding(vertical: topPadding)

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
                                    .buttonStyle(MyButtonStyle())

                            Padding(vertical: bottomPadding)
                        }
                    }
                }
            }
        }
                .frame(maxHeight: sheetHeight)
                .background(bgColor)
                .listStyle(.plain)
                .listSectionSeparatorTint(.clear)
                .onDisappear {
                    sheetActivity = nil
                }
    }
}

private struct MyButtonStyle: ButtonStyle {

    func makeBody(configuration: Self.Configuration) -> some View {
        configuration
                .label
                .frame(height: itemHeight)
                .background(configuration.isPressed ? Color(.systemGray5) : bgColor)
    }
}
