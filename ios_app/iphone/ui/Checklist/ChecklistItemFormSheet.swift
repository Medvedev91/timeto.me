import SwiftUI
import shared

struct ChecklistItemFormSheet: View {
    
    let checklistDb: ChecklistDb
    let checklistItemDb: ChecklistItemDb?
    
    var body: some View {
        VmView({
            ChecklistItemFormVm(
                checklistDb: checklistDb,
                checklistItemDb: checklistItemDb
            )
        }) { vm, state in
            ChecklistItemFormSheetInner(vm: vm, state: state)
        }
    }
}

///

private struct ChecklistItemFormSheetInner: View {
    
    let vm: ChecklistItemFormVm
    let state: ChecklistItemFormVm.State
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        VStack {
            
            HStack {
                
                Button(
                    action: {
                        dismiss()
                    },
                    label: { Text("Cancel") }
                )
                .padding(.leading, 25)
                
                Spacer()
                
                Button(
                    action: {
                        vm.save {
                            dismiss()
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
                    text: state.inputNameValue,
                    placeholder: "Name",
                    isAutofocus: true,
                    onValueChanged: { newValue in
                        vm.setInputName(name: newValue)
                    }
                )
            }
            
            Spacer()
        }
    }
}
