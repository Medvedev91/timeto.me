import SwiftUI
import shared

struct HistoryFormSheet: View {
    
    let initIntervalDb: IntervalDb?
    
    var body: some View {
        VmView({
            HistoryFormVm(
                initIntervalDb: initIntervalDb
            )
        }) { vm, state in
            HistoryFormSheetInner(
                vm: vm,
                state: state,
                selectedActivityDb: state.activityDb
            )
        }
    }
}

///

private struct HistoryFormSheetInner: View {
    
    let vm: HistoryFormVm
    let state: HistoryFormVm.State
    
    @State var selectedActivityDb: ActivityDb
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        List {
            
            Section {
                
                Picker(state.activityTitle, selection: $selectedActivityDb) {
                    ForEach(state.activitiesUi, id: \.activityDb) { activityUi in
                        Text(activityUi.title)
                    }
                }
                .foregroundColor(.primary)
                .onChange(of: selectedActivityDb) { _, newActivityDb in
                    vm.setActivityDb(newActivityDb: newActivityDb)
                }
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.timeTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.timeNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        HistoryFormTimeSheet(
                            initTime: state.time.toInt(),
                            onDone: { newTime in
                                vm.setTime(newTime: newTime.toInt32())
                            }
                        )
                    }
                )
            }
            
            // todo back to tasks list button. Color?
            
            if let intervalDb = state.initIntervalDb {
                Section {
                    Button("Delete") {
                        vm.delete(
                            intervalDb: intervalDb,
                            dialogsManager: navigation,
                            onSuccess: {
                                dismiss()
                            }
                        )
                    }
                    .foregroundColor(.red)
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
                Button(state.saveText) {
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: {
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
            }
        }
    }
}
