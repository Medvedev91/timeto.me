import SwiftUI
import shared

extension NativeSheet {

    func showActivityTimerSheet(
            activity: ActivityModel,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            onStart: @escaping () -> Void
    ) {
        items.append(
                NativeSheet__Item(
                        content: { isPresented in
                            AnyView(
                                    ActivityTimerSheet(
                                            activity: activity,
                                            isPresented: isPresented,
                                            timerContext: timerContext
                                    ) {
                                        isPresented.wrappedValue = false
                                        onStart()
                                    }
                            )
                        }
                )
        )
    }
}

private struct ActivityTimerSheet: View {

    @State private var vm: ActivityTimerSheetVM

    @Binding private var isPresented: Bool
    private let onStart: () -> ()

    @State private var formTimeItemsIdx: Int32 = 0.toInt32()

    init(
            activity: ActivityModel,
            isPresented: Binding<Bool>,
            timerContext: ActivityTimerSheetVM.TimerContext?,
            onStart: @escaping () -> ()
    ) {
        self._isPresented = isPresented
        self.onStart = onStart
        _vm = State(initialValue: ActivityTimerSheetVM(activity: activity, timerContext: timerContext))
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            HStack(spacing: 4) {

                Button(
                        action: { isPresented.toggle() },
                        label: { Text("Cancel") }
                )

                Spacer()

                Text(state.title)
                        .font(.title2)
                        .fontWeight(.semibold)
                        .multilineTextAlignment(.center)

                Spacer()

                Button(
                        action: {
                            vm.start {
                                onStart()
                            }
                        },
                        label: {
                            Text("Start")
                                    .fontWeight(.bold)
                        }
                )
            }
                    .padding(.horizontal, 25)
                    .padding(.top, 24)

            // Plus padding from picker
            if let note = state.note {
                Text(note)
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 6)
            }

            ZStack {

                VStack {

                    Spacer()

                    Picker(
                            "Time",
                            selection: $formTimeItemsIdx
                    ) {
                        ForEach(state.timeItems, id: \.idx) { item in
                            Text(item.title)
                        }
                    }
                            .onChange(of: formTimeItemsIdx) { newValue in
                                vm.setFormTimeItemIdx(newIdx: newValue)
                            }
                            .onAppear {
                                formTimeItemsIdx = state.formTimeItemIdx
                            }
                            .pickerStyle(.wheel)
                            .foregroundColor(.primary)
                            .padding(.bottom, state.note != nil ? 30 : 5)

                    Spacer()
                }

                VStack {

                    Spacer()

                    HStack {
                        TimerHintsView(
                                timerHintsUI: state.timerHints,
                                hintHPadding: 8.0,
                                fontSize: 16.0,
                                fontWeight: .light,
                                onStart: {
                                    onStart()
                                }
                        )
                    }
                            .padding(.bottom, 8)
                }
                        .safeAreaPadding(.bottom)
            }
        }
                .presentationDetentsHeightIf16(400, withDragIndicator: true)
                .ignoresSafeArea()
    }
}
