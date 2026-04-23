import SwiftUI
import shared

struct HistoryFormSheet: View {
    
    let initIntervalDb: IntervalDb
    let withNoteAutoFocus: Bool

    var body: some View {
        VmView({
            HistoryFormVm(
                initIntervalDb: initIntervalDb,
            )
        }) { vm, state in
            let state = vm.state.value as! HistoryFormVm.State
            HistoryFormSheetInner(
                vm: vm,
                state: state,
                withNoteAutoFocus: withNoteAutoFocus,
                note: state.noteInit,
                selectedActivityDb: state.activityDb,
                selectedTime: state.time,
            )
        }
    }
}

private struct HistoryFormSheetInner: View {
    
    let vm: HistoryFormVm
    let state: HistoryFormVm.State
    
    let withNoteAutoFocus: Bool

    @State var note: String
    @State var selectedActivityDb: ActivityDb
    @State var selectedTime: Int32

    ///
    
    @FocusState private var isFocused: Bool
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        List {
            
            TextField(
                state.notePlaceholder,
                text: $note,
            )
            .focused($isFocused)
            
            Section {
                
                Picker(state.activityTitle, selection: $selectedActivityDb) {
                    ForEach(state.activitiesUi, id: \.activityDb) { activityUi in
                        Text(activityUi.title)
                            .tag(activityUi.activityDb)
                    }
                }
                .foregroundColor(.primary)
                .onChange(of: selectedActivityDb) { _, newActivityDb in
                    vm.setActivity(newActivityDb: newActivityDb)
                }
                
                Picker("", selection: $selectedTime) {
                    ForEach(state.timerItemsUi, id: \.time) { itemUi in
                        Text(itemUi.title)
                    }
                }
                .pickerStyle(.wheel)
                .onChange(of: selectedTime) { _, newTime in
                    vm.setTime(newTime: newTime)
                }
            }
        }
        .myFormContentMargins()
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
        .interactiveDismissDisabled()
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button(state.doneText) {
                    vm.save(
                        note: note,
                        dialogsManager: navigation,
                        onSuccess: {
                            dismiss()
                        },
                    )
                }
                .fontWeight(.semibold)
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                
                HStack {
                    
                    Button("Delete") {
                        vm.delete(
                            intervalDb: state.initIntervalDb,
                            dialogsManager: navigation,
                            onSuccess: {
                                dismiss()
                            }
                        )
                    }
                    .foregroundColor(.red)
                    
                    Spacer()
                    
                    Button(HistoryFormUtils.shared.moveToTasksTitle) {
                        vm.moveToTasks(
                            intervalDb: state.initIntervalDb,
                            dialogsManager: navigation,
                            onSuccess: {
                                dismiss()
                            }
                        )
                    }
                    .foregroundColor(.orange)
                }
            }
        }
        .onAppear {
            isFocused = withNoteAutoFocus
        }
    }
}
