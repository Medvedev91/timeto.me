import SwiftUI
import shared

struct RepeatingFormPeriodSheet: View {
    
    let initPeriod: RepeatingDbPeriod?
    let onDone: (RepeatingDbPeriod) -> Void
    
    var body: some View {
        VmView({
            RepeatingFormPeriodVm(
                initPeriod: initPeriod
            )
        }) { vm, state in
            RepeatingFormPeriodSheetInner(
                vm: vm,
                state: state,
                activePeriodIdx: state.activePeriodIdx,
                selectedNDays: state.selectedNDays,
                onDone: onDone
            )
        }
    }
}

private struct RepeatingFormPeriodSheetInner: View {
    
    let vm: RepeatingFormPeriodVm
    let state: RepeatingFormPeriodVm.State
    
    @State var activePeriodIdx: Int32
    @State var selectedNDays: Int32
    
    let onDone: (RepeatingDbPeriod) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        List {
            
            Section {
                
                Picker("Type", selection: $activePeriodIdx) {
                    ForEach(state.periodPickerItemsUi, id: \.idx) { itemUi in
                        Text(itemUi.title)
                    }
                }
                .foregroundColor(.primary)
                .onChange(of: activePeriodIdx) { _, newActivePeriodIdx in
                    vm.setActivePeriodIdx(newIdx: newActivePeriodIdx)
                }
            }
            
            if state.activePeriodIdx == 1 {
                Section {
                    Picker("", selection: $selectedNDays) {
                        ForEach(2..<667, id: \.self) {
                            Text("\($0)").tag($0.toInt32())
                        }
                    }
                    .pickerStyle(.wheel)
                    .onChange(of: selectedNDays) { _, newValue in
                        vm.setSelectedNDays(newNDays: newValue.toInt32())
                    }
                }
            }
            else if state.activePeriodIdx == 2 {
                Section {
                    ForEach(state.daysOfWeekUi, id: \.idx) { dayOfWeekUi in
                        Button(
                            action: {
                                vm.toggleDayOfWeek(dayOfWeekIdx: dayOfWeekUi.idx)
                            },
                            label: {
                                HStack {
                                    Text(dayOfWeekUi.title)
                                        .foregroundColor(.primary)
                                    Spacer()
                                    if state.selectedDaysOfWeek.toSwift().contains(dayOfWeekUi.idx.toInt()) {
                                        Image(systemName: "checkmark")
                                            .foregroundColor(.blue)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        .myFormContentMargins()
        .interactiveDismissDisabled()
        .navigationTitle(state.title)
        .toolbarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    // todo onDone()
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
    }
}
