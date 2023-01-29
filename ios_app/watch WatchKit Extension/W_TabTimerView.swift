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
                Text(state.timeNote)
                        .font(.system(size: 28, design: .monospaced))
                        .fontWeight(.medium)
                        .foregroundColor(state.color.toColor())
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

        private var isActive: Bool {
            activityUI.activity.id == lastInterval.activity_id
        }

        let defBgColor = Color(rgba: [34, 34, 35])

        var body: some View {
            Button(
                    action: {
                        isTickerPresented = true
                    },
                    label: {
                        VStack(spacing: 0) {

                            Text(activityUI.listTitle)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .lineLimit(1)
                                    .truncationMode(.middle)

                            if isActive, let note = lastInterval.note {

                                HStack(spacing: 0) {

                                    Text(note.removeTriggerIdsNoEnsure())
                                            .font(.system(size: 14, weight: .light))
                                            .lineLimit(1)
                                            .truncationMode(.middle)

                                    Button(
                                            action: {
                                                withAnimation {
                                                    WatchToIosSync.shared.cancelWithLocal()
                                                }
                                            },
                                            label: {
                                                Text("cancel")
                                                        .font(.system(size: 13, weight: .medium))
                                                        .foregroundColor(.blue)
                                                        .padding(.leading, 7)
                                                        .padding(.trailing, 8)
                                                        .padding(.top, 2)
                                                        .padding(.bottom, 3.5)
                                                        .background(.white)
                                                        .clipShape(Capsule())
                                                        .padding(.leading, 8)

                                            }
                                    )
                                            .buttonStyle(.borderless)

                                    Spacer(minLength: 0)
                                }
                            }

                            if !activityUI.timerHints.isEmpty {
                                HStack {
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
                                    .fill(isActive ? .blue : defBgColor)
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
