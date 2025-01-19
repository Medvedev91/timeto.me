import SwiftUI
import shared

struct NoteFormSheet: View {
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    
    @Binding private var isPresented: Bool
    @State private var vm: NoteFormSheetVm
    private let onDelete: () -> Void
    
    init(
        isPresented: Binding<Bool>,
        note: NoteDb?,
        onDelete: @escaping () -> Void
    ) {
        _isPresented = isPresented
        _vm = State(initialValue: NoteFormSheetVm(note: note))
        self.onDelete = onDelete
    }
    
    var body: some View {
        
        VMView(vm: vm, stack: .VStack()) { state in
            
            Sheet__HeaderView(
                title: state.headerTitle,
                scrollToHeader: 0,
                bgColor: c.sheetBg
            )
            
            MyListView__ItemView(
                isFirst: true,
                isLast: true
            ) {
                
                MyListView__ItemView__TextInputView(
                    text: state.inputTextValue,
                    placeholder: state.inputTextPlaceholder,
                    isAutofocus: false,
                    onValueChanged: { newText in
                        vm.setInputText(newText: newText)
                    }
                )
            }
            .padding(.bottom, H_PADDING)
            
            Spacer()
            
            Sheet__BottomViewDefault(
                primaryText: state.doneTitle,
                primaryAction: {
                    vm.save {
                        isPresented = false
                    }
                },
                secondaryText: "Cancel",
                secondaryAction: {
                    isPresented = false
                },
                topContent: { EmptyView() },
                startContent: {
                    
                    ZStack {
                        
                        if let deleteFun = state.deleteFun {
                            
                            ZStack {
                                
                                Button(
                                    action: {
                                        deleteFun {
                                            // todo don't use to fast hide parent sheet
                                            isPresented = false
                                            onDelete()
                                            return KotlinUnit() // WTF?
                                        }
                                    },
                                    label: {
                                        ZStack {
                                            Image(systemName: "trash")
                                                .font(.system(size: 20, weight: .medium))
                                                .foregroundColor(c.red)
                                        }
                                        .frame(width: 34, height: 34)
                                    }
                                )
                            }
                            .padding(.leading, H_PADDING)
                        } else {
                            EmptyView()
                        }
                    }
                }
            )
        }
    }
}
