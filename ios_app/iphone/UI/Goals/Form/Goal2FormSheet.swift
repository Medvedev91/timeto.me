import SwiftUI
import shared

struct Goal2FormSheet: View {
    
    let goalDb: Goal2Db?
    
    var body: some View {
        VmView({
            Goal2FormVm(
                initGoalDb: goalDb
            )
        }) { vm, state in
            Goal2FormSheetInner(
                vm: vm,
                state: state,
                name: state.name,
                seconds: state.seconds,
                secondsNote: state.secondsNote,
            )
        }
    }
}

private struct Goal2FormSheetInner: View {
    
    let vm: Goal2FormVm
    let state: Goal2FormVm.State
    
    @State var name: String
    @State var seconds: Int32
    @State var secondsNote: String
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @FocusState private var focusedField: FocusedField?
    
    @State private var isSecondsPickerExpanded = false
    
    var body: some View {
        List {
            
            Section {
                
                TextField(
                    state.namePlaceholder,
                    text: $name
                )
                .focused($focusedField, equals: .name)
                .onChange(of: name) { _, newName in
                    vm.setName(newName: newName)
                }
                
                NavigationLinkAction(
                    label: {
                        HStack {
                            Text(state.secondsTitle)
                            Spacer()
                            Text(secondsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    action: {
                        withAnimation {
                            isSecondsPickerExpanded.toggle()
                        }
                    },
                )
                .animateVmValue(vmValue: state.secondsNote, swiftState: $secondsNote)
                
                if isSecondsPickerExpanded {
                    Picker("", selection: $seconds) {
                        ForEach(state.secondsPickerItemsUi, id: \.seconds) { itemUi in
                            Text(itemUi.title)
                        }
                    }
                    .pickerStyle(.wheel)
                    .onChange(of: seconds) { _, newSeconds in
                        vm.setSeconds(newSeconds: newSeconds)
                    }
                }
            }
        }
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(state.doneText) {
                }
                .fontWeight(.semibold)
                .disabled(!state.isDoneEnabled)
            }
        }
        .onAppear {
            focusedField = .name
        }
    }
}

private enum FocusedField {
    case name
}
