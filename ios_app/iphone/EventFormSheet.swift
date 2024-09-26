import SwiftUI
import shared

extension Fs {

    func EventFormSheet__show(
        editedEvent: EventDb?,
        defText: String? = nil,
        defTime: Int? = nil,
        onSave: @escaping () -> Void
    ) {
        self.show { isPresented in
            EventFormSheet(
                isPresented: isPresented,
                editedEvent: editedEvent,
                defText: defText ?? "",
                defTime: defTime
            ) {
                onSave()
            }
        }
    }
}

private struct EventFormSheet: View {

    @State private var vm: EventFormSheetVm

    @Binding private var isPresented: Bool
    private let onSave: () -> Void

    @State private var date = Date()

    @State private var sheetHeaderScroll = 0

    init(
        isPresented: Binding<Bool>,
        editedEvent: EventDb?,
        defText: String? = nil,
        defTime: Int? = nil,
        onSave: @escaping () -> Void = {
        }
    ) {
        _vm = State(initialValue: EventFormSheetVm(
            event: editedEvent,
            defText: defText,
            defTime: defTime?.toKotlinInt()
        ))
        self.onSave = onSave
        _isPresented = isPresented
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            ZStack {
            }
            .onAppear {
                date = Date(timeIntervalSince1970: Double(state.selectedUnixTime.time))
            }
            .onChange(of: date) { _ in
                vm.setUnixDay(unixDay: date.toUnixTime().localDay)
                hideKeyboard()
            }

            Fs__HeaderClose {
                isPresented = false
            }

            GeometryReader { geometry in

                ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                    VStack {

                        Spacer()

                        HStack {

                            DatePicker(
                                "Start Date",
                                selection: $date,
                                in: Date().startOfDay()...,
                                displayedComponents: [.date]
                            )
                                .labelsHidden()
                                .datePickerStyle(.compact)
                                .padding(.leading, H_PADDING)

                            Spacer()
                        }

                        DaytimePickerSliderView(
                            daytimeUi: state.daytimeUi,
                            onChange: { daytimeUi in
                                vm.setDaytime(daytimeUi: daytimeUi)
                            }
                        )
                        .padding(.top, 16)
                        .padding(.bottom, 12)

                        EventTemplatesView(
                            onPick: { templateUi in
                                vm.setTemplate(templateUi: templateUi)
                            }
                        )
                        .padding(.bottom, 10)
                    }
                    .frame(minHeight: geometry.size.height)
                }
            }

            HStack {

                MyListView__ItemView__TextInputView(
                    text: state.inputTextValue,
                    placeholder: state.inputPlaceholder,
                    isAutofocus: true
                ) { newValue in
                    vm.setInputTextValue(text: newValue)
                }

                Button(
                    action: {
                        vm.save {
                            onSave()
                            isPresented = false
                        }
                    },
                    label: {
                        Text(state.saveText)
                            .font(.system(size: 16, weight: .medium))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .foregroundColor(c.text)
                            .background(roundedShape.fill(c.blue))
                    }
                )
                .padding(.leading, 8)
                .padding(.trailing, H_PADDING)
            }
            .padding(.vertical, 4)
            .background(c.fg)
        }
        .background(c.bg)
    }
}
