import SwiftUI
import shared

struct W_TabTimerView: View {

    @State private var vm = WatchTabTimerVM()

    var body: some View {

        VMView(vm: vm) { state in

            ScrollViewReader { scrollProxy in

                VStack(spacing: 0) {

                    TimerView()
                            .offset(y: -2)
                            // The height on the devices can be different, set approximately.
                            .frame(minHeight: 30, maxHeight: 30)

                    // Tried by List, but listRowBackground() does not work on screen transition.
                    ScrollView(.vertical, showsIndicators: false) {
                        VStack(spacing: 5) {
                            ForEach(state.activitiesUI, id: \.activity.id) { activityUI in
                                ActivityView(activityUI: activityUI, lastInterval: state.lastInterval)
                                        .id("aid__\(activityUI.activity.id)")
                            }
                        }
                    }
                            .animation(.default)
                            .padding(.top, 4)
                }
                        .onChange(of: state.lastInterval) { newLastInterval in
                            scrollProxy.scrollTo("aid__\(newLastInterval.activity_id)", anchor: .top)
                        }
            }
        }
    }

    struct TimerView: View {

        @State private var vm = WatchTimerVM()

        var body: some View {
            VMView(vm: vm, stack: .ZStack()) { state in
                Text(state.timerData.title)
                        .font(.system(size: 28, design: .monospaced))
                        .fontWeight(.medium)
                        .foregroundColor(state.timerData.color.toColor())
            }
                    .onTapGesture {
                        vm.toggleIsCountDown()
                    }
        }
    }

    struct ActivityView: View {

        var activityUI: WatchTabTimerVM.ActivityUI
        var lastInterval: IntervalModel
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

                                VStack {
                                    Text(activityUI.data.listText)
                                            .frame(maxWidth: .infinity, alignment: .leading)
                                            .lineLimit(1)
                                            .truncationMode(.middle)

                                    if let listNote = activityUI.data.listNote {

                                        Text(listNote)
                                                .frame(maxWidth: .infinity, alignment: .leading)
                                                .font(.system(size: 14, weight: .light))
                                    }
                                }

                                if activityUI.data.isPauseEnabled {

                                    Button(
                                            action: {
                                                WatchToIosSync.shared.pauseWithLocal()
                                            },
                                            label: {
                                                Image(systemName: "pause.fill")
                                                        .foregroundColor(.blue)
                                                        .font(.system(size: 16))
                                            }
                                    )
                                            .buttonStyle(.borderless)
                                            .frame(width: 30, height: 30)
                                            .background(roundedShape.fill(.white))
                                            .padding(.leading, 6)
                                }
                            }

                            if !activityUI.timerHints.isEmpty {
                                HStack(spacing: 6) {
                                    ForEach(activityUI.timerHints, id: \.seconds) { hintUI in
                                        Button(
                                                action: {
                                                    hintUI.startInterval {}
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
                    .background(
                            RoundedRectangle(cornerRadius: 10, style: .continuous)
                                    // На глаз с элементами из списка задач
                                    .fill(activityUI.data.isActive ? .blue : defBgColor)
                    )
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
