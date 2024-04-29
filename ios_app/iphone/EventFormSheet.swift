import SwiftUI
import shared

extension NativeSheet {

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

    @State private var vm: EventFormSheetVM

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
        _vm = State(initialValue: EventFormSheetVM(
            event: editedEvent,
            defText: defText,
            defTime: defTime?.toKotlinInt()
        ))
        self.onSave = onSave
        _isPresented = isPresented
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            SheetHeaderView(
                onCancel: { isPresented.toggle() },
                title: state.headerTitle,
                doneText: state.saveText,
                isDoneEnabled: true,
                scrollToHeader: sheetHeaderScroll
            ) {
                vm.save {
                    onSave()
                    isPresented = false
                }
            }
            .onAppear {
                date = Date(timeIntervalSince1970: Double(state.selectedUnixTime.time))
            }
            .onChange(of: date) { _ in
                vm.setUnixDay(unixDay: date.toUnixTime().localDay)
                hideKeyboard()
            }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                VStack {

                    MyListView__ItemView(
                        isFirst: true,
                        isLast: true
                    ) {

                        MyListView__ItemView__TextInputView(
                            text: state.inputTextValue,
                            placeholder: "Title",
                            // Autofocus only for new events
                            isAutofocus: true
                        ) { newValue in
                            vm.setInputTextValue(text: newValue)
                        }
                    }
                    .padding(.top, 1)

                    HStack {

                        DatePicker(
                            "Start Date",
                            selection: $date,
                            in: Date().startOfDay()...,
                            displayedComponents: [.date]
                        )
                            .labelsHidden()
                            .padding(.top, 8)
                            .datePickerStyle(.compact)

                        Spacer()
                    }
                    .padding(.leading, H_PADDING + 4)

                    Spacer()
                        .frame(minHeight: 20)
                }
            }
        }
        .background(c.sheetBg)
    }
}
