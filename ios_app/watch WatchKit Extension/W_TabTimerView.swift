import SwiftUI
import shared

struct W_TabTimerView: View {

    @State private var vm = WatchTabTimerVm()

    var body: some View {

        VMView(vm: vm) { state in

            ScrollViewReader { scrollProxy in

                VStack {

                    // Tried by List, but listRowBackground() does not work on screen transition.
                    ScrollView(.vertical, showsIndicators: false) {

                        VStack(spacing: 5) {

                            TimerView()
                                .id("timer_view")
                                .padding(.bottom, 16)

                            ForEach(state.activitiesUI, id: \.activity.id) { activityUI in
                                ActivityView(activityUI: activityUI)
                                    .id("aid__\(activityUI.activity.id)")
                            }
                        }
                    }
                    .animation(.default)
                }
                .onChange(of: state.lastInterval) { newLastInterval in
                    scrollProxy.scrollTo("timer_view", anchor: .top)
                }
            }
        }
    }

    struct TimerView: View {

        @State private var vm = WatchTimerVm()

        var body: some View {

            VMView(vm: vm, stack: .VStack()) { state in

                Text(state.timerData.note)
                    .foregroundColor(state.timerData.noteColor.toColor())
                    .font(.system(size: 15))

                Text(state.timerData.timerText)
                    .font(.system(size: 35, weight: .bold, design: .monospaced))
                    .fontWeight(.medium)
                    .foregroundColor(state.timerData.timerColor.toColor())
            }
            .onTapGesture {
                vm.togglePomodoro()
            }
        }
    }

    struct ActivityView: View {

        var activityUI: WatchTabTimerVm.ActivityUI
        @State private var isTickerPresented = false

        let defBgColor = Color(rgba: [34, 34, 35])

        var body: some View {

            Button(
                action: {
                    isTickerPresented = true
                },
                label: {

                    VStack {

                        HStack(alignment: .center) {

                            Text(activityUI.text)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .lineLimit(1)
                                .truncationMode(.middle)
                        }

                        if !activityUI.timerHints.isEmpty {
                            HStack(spacing: 6) {
                                ForEach(activityUI.timerHints, id: \.seconds) { hintUI in
                                    Button(
                                        action: {
                                            hintUI.startInterval {
                                            }
                                        },
                                        label: {
                                            Text(hintUI.text)
                                                .font(.system(size: 13, weight: .medium))
                                                .foregroundColor(.white)
                                        }
                                    )
                                    .buttonStyle(.borderless)
                                }
                                Spacer()
                            }
                        }
                    }
                    .background(defBgColor.opacity(0.001))
                }
            )
            .frame(minHeight: 36) // Approximately
            .padding([.horizontal], 8)
            .padding([.vertical], 4)
            .buttonStyle(.plain)
            .background(squircleShape.fill(defBgColor))
            .sheet(isPresented: $isTickerPresented) {
                W_TickerDialog(
                    activity: activityUI.activity,
                    task: nil
                ) {
                    isTickerPresented = false
                }
            }
        }
    }
}
