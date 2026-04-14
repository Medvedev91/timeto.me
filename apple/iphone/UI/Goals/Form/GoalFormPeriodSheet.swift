import SwiftUI
import shared

struct GoalFormPeriodSheet: View {
    
    let initActivityDbPeriod: ActivityDbPeriod
    let onDone: (ActivityDbPeriod) -> Void
    
    var body: some View {
        VmView({
            GoalFormPeriodVm(
                initActivityDbPeriod: initActivityDbPeriod,
            )
        }) { vm, state in
            GoalFormPeriodSheetInner(
                vm: vm,
                state: state,
                onDone: onDone,
            )
        }
    }
}

private struct GoalFormPeriodSheetInner: View {
    
    let vm: GoalFormPeriodVm
    let state: GoalFormPeriodVm.State
    let onDone: (ActivityDbPeriod) -> Void

    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        List {
            Section {
                ForEach(vm.daysOfWeek, id: \.id) { dayOfWeek in
                    Button(
                        action: {
                            vm.toggleDayOfWeek(dayOfWeek: dayOfWeek)
                        },
                        label: {
                            HStack {
                                Text(dayOfWeek.title)
                                    .foregroundColor(.primary)
                                Spacer()
                                if state.selectedDaysOfWeek.contains(dayOfWeek.id.toKotlinInt()) {
                                    Image(systemName: "checkmark")
                                }
                            }
                        }
                    )
                }
            }
        }
        .myFormContentMargins()
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(state.doneText) {
                    guard let period = state.buildPeriodOrNull(
                        dialogsManager: navigation
                    ) else { return }
                    onDone(period)
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
