import SwiftUI
import shared

struct ChecklistFormSheet: View {

    @State private var vm: ChecklistFormVM

    @Binding private var isPresented: Bool

    init(
            isPresented: Binding<Bool>,
            checklist: ChecklistModel?
    ) {
        _isPresented = isPresented
        vm = ChecklistFormVM(checklist: checklist)
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
                            vm.save {
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

            MyListView__SectionView {

                MyListView__SectionView__TextInputView(
                        text: state.inputNameValue,
                        placeholder: "Name",
                        isAutofocus: true,
                        onValueChanged: { newValue in
                            vm.setInputName(name: newValue)
                        }
                )
            }
                    .padding(.top, MyListView.PADDING_HEADER_SECTION)

            Spacer()
        }
                .background(Color(.mySheetFormBg))
    }
}
