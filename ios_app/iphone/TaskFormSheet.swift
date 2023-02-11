import SwiftUI
import shared

struct TaskFormSheet: View {

    @State private var vm: TaskFormSheetVM
    @Binding private var isPresented: Bool

    @State private var triggersBg = UIColor.myDayNight(.white, .mySheetFormBg)

    init(
            task: TaskModel,
            isPresented: Binding<Bool>
    ) {
        _vm = State(initialValue: TaskFormSheetVM(task: task))
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

            TriggersView__Form(
                    triggers: state.textFeatures.triggers,
                    onTriggersChanged: { newTriggers in
                        vm.setTriggers(newTriggers: newTriggers)
                    },
                    spaceAround: MyListView.PADDING_OUTER_HORIZONTAL,
                    bgColor: triggersBg,
                    paddingTop: 20
            )

            Spacer()
        }
                .background(Color(.mySheetFormBg))
    }
}
