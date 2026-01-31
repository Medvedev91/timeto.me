import SwiftUI
import shared

struct RepeatingFormSheet: View {
    
    let initRepeatingDb: RepeatingDb?
    
    var body: some View {
        VmView({
            RepeatingFormVm(
                initRepeatingDb: initRepeatingDb
            )
        }) { vm, state in
            RepeatingFormSheetInner(
                vm: vm,
                state: state,
                text: state.text,
                goalDb: state.goalDb,
                isImportant: state.isImportant,
                inCalendar: state.inCalendar,
            )
        }
    }
}

private struct RepeatingFormSheetInner: View {
    
    let vm: RepeatingFormVm
    let state: RepeatingFormVm.State
    
    @State var text: String
    @State var goalDb: Goal2Db?
    @State var isImportant: Bool
    @State var inCalendar: Bool
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        Form {
            
            Section {
                TextField(
                    state.textPlaceholder,
                    text: $text
                )
                .onChange(of: text) { _, newText in
                    vm.setText(newText: newText)
                }
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.periodTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.periodNote)
                                .foregroundColor(state.period != nil ? .secondary : .red)
                        }
                    },
                    sheet: {
                        RepeatingFormPeriodSheet(
                            initPeriod: state.period,
                            onDone: { newPeriod in
                                vm.setPeriod(newPeriod: newPeriod)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.daytimeTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.daytimeNote)
                                .foregroundColor(state.daytimeUi == nil ? .red : .secondary)
                        }
                    },
                    sheet: {
                        DaytimePickerSheet(
                            title: state.daytimeTitle,
                            doneText: "Done",
                            daytimeUi: state.daytimePickerUi,
                            onDone: { daytimeUi in
                                vm.setDaytime(newDaytimeUi: daytimeUi)
                            },
                            onRemove: nil
                        )
                    }
                )
            }
            
            Section {
                
                Picker(state.goalTitle, selection: $goalDb) {
                    if goalDb == nil {
                        Text("None")
                            .tag(nil as Goal2Db?) // Support optional (nil) selection
                    }
                    ForEach(state.goalsUi, id: \.goalDb) { goalUi in
                        Text(goalUi.title)
                            .tag(goalUi.goalDb as Goal2Db?) // Support optional (nil) selection
                    }
                }
                .pickerStyle(.menu)
                .accentColor(goalDb == nil ? .red : .secondary)
                .foregroundColor(.primary)
                .onChange(of: goalDb) { _, newGoalDb in
                    vm.setGoal(newGoalDb: newGoalDb)
                }
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.timerTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.timerNote)
                                .foregroundColor(state.timerSeconds == nil ? .red : .secondary)
                        }
                    },
                    sheet: {
                        TimerSheet(
                            title: state.timerTitle,
                            doneTitle: "Done",
                            initSeconds: state.timerPickerSeconds.toInt(),
                            hints: state.goalDb?.buildTimerHints().toIntList() ?? [],
                            onDone: { newTimerSeconds in
                                vm.setTimerSeconds(newTimerSeconds: newTimerSeconds.toInt32())
                            }
                        )
                        .interactiveDismissDisabled()
                    }
                )
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.checklistsTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.checklistsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ChecklistsPickerSheet(
                            initChecklistsDb: state.checklistsDb,
                            onDone: { newChecklistsDb in
                                vm.setChecklists(newChecklistsDb: newChecklistsDb)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.shortcutsTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.shortcutsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ShortcutsPickerSheet(
                            initShortcutsDb: state.shortcutsDb,
                            onDone: { newShortcutsDb in
                                vm.setShortcuts(newShortcutsDb: newShortcutsDb)
                            }
                        )
                    }
                )
            }
            
            Section {
                
                Toggle("Display in Calendar", isOn: $inCalendar)
                    .onChange(of: inCalendar) { _, newInCalendar in
                        vm.setInCalendar(newInCalendar: newInCalendar)
                    }
                
                Toggle(state.isImportantTitle, isOn: $isImportant)
                    .onChange(of: isImportant) { _, newIsImportant in
                        vm.setIsImportant(newIsImportant: newIsImportant)
                    }
            }
            
            if let repeatingDb = state.initRepeatingDb {
                Section {
                    Button("Delete Repeating Task") {
                        vm.delete(
                            repeatingDb: repeatingDb,
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
        }
    }
}
