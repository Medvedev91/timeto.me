import SwiftUI
import shared

struct TaskFormSheet: View {

    @State private var vm: TaskFormSheetVm
    @Binding private var isPresented: Bool

    init(
            task: TaskDb?,
            isPresented: Binding<Bool>
    ) {
        _vm = State(initialValue: TaskFormSheetVm(task: task))
        _isPresented = isPresented
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: state.headerDoneText,
                    isDoneEnabled: state.isHeaderDoneEnabled,
                    scrollToHeader: 0
            ) {
                vm.save {
                    isPresented = false
                }
            }

            MyListView__ItemView(
                    isFirst: true,
                    isLast: true
            ) {

                MyListView__ItemView__TextInputView(
                        text: state.inputTextValue,
                        placeholder: "Task",
                        isAutofocus: true
                ) { newValue in
                    vm.setInputTextValue(text: newValue)
                }
            }
                    .padding(.top, 10)

            MyListView__Padding__SectionSection()

            TextFeaturesTimerFormView(
                    textFeatures: state.textFeatures
            ) { textFeatures in
                vm.setTextFeatures(newTextFeatures: textFeatures)
            }

            MyListView__Padding__SectionSection()

            TextFeaturesTriggersFormView(
                    textFeatures: state.textFeatures
            ) { textFeatures in
                vm.setTextFeatures(newTextFeatures: textFeatures)
            }

            Spacer()
        }
                .background(c.sheetBg)
    }
}
