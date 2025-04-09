import SwiftUI
import shared

struct ActivityFormGoalsSheet: View {
    
    let initGoalFormsData: [GoalFormData]
    let onDone: ([GoalFormData]) -> Void
    
    var body: some View {
        VmView({
            ActivityFormGoalsVm(
                initGoalFormsData: initGoalFormsData
            )
        }) { vm, state in
            ActivityFormGoalsSheetInner(
                vm: vm,
                state: state,
                onDone: onDone
            )
        }
    }
}

private struct ActivityFormGoalsSheetInner: View {
    
    let vm: ActivityFormGoalsVm
    let state: ActivityFormGoalsVm.State
    
    let onDone: ([GoalFormData]) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        List {
            ForEach(Array(state.goalFormsData.enumerated()), id: \.offset) { idx, goalFormData in
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(goalFormData.formListTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(goalFormData.formListNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        GoalFormSheet(
                            strategy: GoalFormStrategy.EditFormData(
                                initGoalFormData: goalFormData,
                                onDone: { newGoalFormData in
                                    vm.updateGoalFormData(
                                        idx: idx.toInt32(),
                                        new: newGoalFormData
                                    )
                                },
                                onDelete: {
                                    vm.deleteGoalFormData(idx: idx.toInt32())
                                }
                            )
                        )
                    }
                )
            }
        }
        .navigationTitle("Goals")
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    onDone(state.goalFormsData)
                    dismiss()
                }
                .fontWeight(.semibold)
            }
            
            ToolbarItemGroup(placement: .bottomBar) {
                
                BottomBarAddButton(
                    text: state.newGoalTitle,
                    action: {
                        navigation.sheet {
                            GoalFormSheet(
                                strategy: vm.newGoalStrategy
                            )
                        }
                    }
                )
                
                Spacer()
            }
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
    }
}
