import SwiftUI
import shared

struct HistoryFormSheet: View {
    
    let initIntervalDb: IntervalDb
    
    var body: some View {
        VmView({
            HistoryFormVm(
                initIntervalDb: initIntervalDb
            )
        }) { vm, state in
            HistoryFormSheetInner(
                vm: vm,
                state: state,
                selectedGoalDb: state.goalDb,
                selectedTime: state.time
            )
        }
    }
}

private struct HistoryFormSheetInner: View {
    
    let vm: HistoryFormVm
    let state: HistoryFormVm.State
    
    @State var selectedGoalDb: Goal2Db
    @State var selectedTime: Int32

    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        List {
            
            Section {
                
                Picker(state.goalTitle, selection: $selectedGoalDb) {
                    ForEach(state.goalsUi, id: \.goalDb) { goalUi in
                        Text(goalUi.title)
                            .tag(goalUi.goalDb)
                    }
                }
                .foregroundColor(.primary)
                .onChange(of: selectedGoalDb) { _, newGoalDb in
                    vm.setGoal(newGoalDb: newGoalDb)
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
                        dialogsManager: navigation,
                        onSuccess: {
                            dismiss()
                        }
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
    }
}
