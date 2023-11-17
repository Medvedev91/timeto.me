import SwiftUI
import shared

extension NativeSheet {

    func EventFormSheet__show(
            editedEvent: EventModel?,
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

struct EventFormSheet: View {

    @State private var vm: EventFormSheetVM

    @Binding private var isPresented: Bool
    private let onSave: () -> Void

    @State private var date = Date()

    @State private var sheetHeaderScroll = 0

    init(
            isPresented: Binding<Bool>,
            editedEvent: EventModel?,
            defText: String? = nil,
            defTime: Int? = nil,
            onSave: @escaping () -> Void = {}
    ) {
        _vm = State(initialValue: EventFormSheetVM(
                event: editedEvent,
                defText: defText,
                defTime: defTime?.toKotlinInt()
        ))
        self.onSave = onSave
        _isPresented = isPresented
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: state.headerDoneText,
                    isDoneEnabled: state.isHeaderDoneEnabled,
                    scrollToHeader: sheetHeaderScroll
            ) {
                vm.save {
                    onSave()
                    isPresented = false
                }
            }
                    .onAppear {
                        date = Date(timeIntervalSince1970: Double(state.selectedTime))
                    }
                    .onChange(of: date) { _ in
                        vm.setTime(time: date.toUnixTime().time)
                        hideKeyboard()
                    }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack(spacing: 0) {

                    MyListView__ItemView(
                            isFirst: true,
                            isLast: true
                    ) {

                        MyListView__ItemView__TextInputView(
                                text: state.inputTextValue,
                                placeholder: "Title",
                                // Autofocus only for new events
                                isAutofocus: state.isAutoFocus
                        ) { newValue in
                            vm.setInputTextValue(text: newValue)
                        }
                    }
                            .padding(.top, 1)

                    DatePicker(
                            "Start Date",
                            selection: $date,
                            in: Date().startOfDay()...,
                            displayedComponents: [.date]
                    )
                            .labelsHidden()
                            .padding(.horizontal, 28)
                            .padding(.top, 8)
                            .datePickerStyle(.graphical)

                    DatePicker(
                            "Start Date",
                            selection: $date,
                            displayedComponents: [.hourAndMinute]
                    )
                            .labelsHidden()
                            .datePickerStyle(.wheel)

                    Spacer()
                            .frame(minHeight: 20)
                }
            }
        }
                .background(c.sheetBg)
    }
}
