import SwiftUI
import shared

struct NoteFolderFormSheet: View {
    
    let noteFolderDb: NoteFolderDb?
    let onDelete: () -> Void

    var body: some View {
        
        VmView({
            NoteFolderFormVm(noteFolderDb: noteFolderDb)
        }) { vm, state in
            let state = vm.state.value as! NoteFolderFormVm.State
            NoteFolderFormSheetInner(
                vm: vm,
                state: state,
                name: state.name,
                onHome: state.onHome,
                onDelete: onDelete,
            )
        }
    }
}

private struct NoteFolderFormSheetInner: View {
    
    let vm: NoteFolderFormVm
    let state: NoteFolderFormVm.State
    
    @State var name: String
    @State var onHome: Bool
    
    let onDelete: () -> Void
    
    ///
    
    @FocusState private var isFocused: Bool
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        List {
            
            TextField(
                state.namePlaceholder,
                text: $name,
            )
            .focused($isFocused)
            .onChange(of: name) { _, new in
                vm.setName(name: new)
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.iconTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            if let symbol = state.symbol {
                                FormButtonSymbolView(
                                    symbol: symbol,
                                    color: .secondary,
                                )
                            } else {
                                Text("Not Selected")
                                    .foregroundColor(.red)
                            }
                        }
                    },
                    sheet: {
                        SymbolPickerSheet(
                            onPick: { symbol in
                                vm.setSymbol(symbol: symbol)
                            }
                        )
                    },
                )
                
                Toggle(
                    state.onHomeTitle,
                    isOn: $onHome,
                )
                .onChange(of: onHome) { _, newValue in
                    vm.setOnHome(onHome: newValue)
                }
            }
            
            if let noteFolderDb = state.noteFolderDb {
                Section {
                    Button(state.deleteText) {
                        vm.delete(
                            noteFolderDb: noteFolderDb,
                            dialogsManager: navigation,
                            onDelete: {
                                dismiss()
                                onDelete()
                            },
                        )
                    }
                    .foregroundColor(.red)
                }
            }
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.title)
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button(state.doneText) {
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: {
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
                .disabled(!state.isSaveEnabled)
            }
        }
        .onAppear {
            isFocused = true
        }
    }
}
