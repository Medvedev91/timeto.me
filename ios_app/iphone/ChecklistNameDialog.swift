import SwiftUI
import shared

struct ChecklistNameDialog: View {

    @State private var vm: ChecklistNameDialogVm

    @Binding private var isPresented: Bool
    private let onSave: (ChecklistDb) -> Void

    init(
        isPresented: Binding<Bool>,
        checklist: ChecklistDb?,
        onSave: @escaping (ChecklistDb) -> Void
    ) {
        _isPresented = isPresented
        vm = ChecklistNameDialogVm(checklistDb: checklist)
        self.onSave = onSave
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            HStack {

                Button(
                    action: {
                        isPresented = false
                    },
                    label: { Text("Cancel") }
                )
                .padding(.leading, 25)

                Spacer()

                Button(
                    action: {
                        vm.save { checklist in
                            onSave(checklist)
                            isPresented = false
                        }
                    },
                    label: {
                        Text("Save")
                            .fontWeight(.heavy)
                            .padding(.trailing, 25)
                    }
                )
                .disabled(!state.isSaveEnabled)
            }
            .padding(.top, 20)

            MyListView__Padding__SectionSection()

            MyListView__ItemView(
                isFirst: true,
                isLast: true
            ) {

                MyListView__ItemView__TextInputView(
                    text: state.input,
                    placeholder: "Name",
                    isAutofocus: true,
                    onValueChanged: { newValue in
                        vm.setInput(value: newValue)
                    }
                )
            }

            Spacer()
        }
        .background(c.sheetBg)
    }
}
