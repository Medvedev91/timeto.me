import SwiftUI
import shared

struct GoalFormSheet: View {
    
    let strategy: GoalFormStrategy
    
    var body: some View {
        VmView({
            GoalFormVm(
                strategy: strategy
            )
        }) { vm, state in
            GoalFormSheetInner(
                vm: vm,
                state: state,
                strategy: strategy,
                note: state.note
            )
        }
    }
}

private struct GoalFormSheetInner: View {
    
    let vm: GoalFormVm
    let state: GoalFormVm.State
    let strategy: GoalFormStrategy
    
    @State var note: String
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        List {
            
            Section {
                
                TextField(
                    state.notePlaceholder,
                    text: $note
                )
                .onChange(of: note) { _, new in
                    vm.setNote(newNote: new)
                }
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.periodTitle)
                            Spacer()
                            Text(state.periodNote)
                                .foregroundColor(state.period == nil ? .red : .secondary)
                        }
                    },
                    sheet: {
                        GoalFormPeriodSheet(
                            initGoalDbPeriod: state.period,
                            onDone: { newPeriod in
                                vm.setPeriod(newPeriod: newPeriod)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.secondsTitle)
                            Spacer()
                            Text(state.secondsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        TimerSheet(
                            title: state.secondsTitle,
                            doneTitle: "Done",
                            initSeconds: state.seconds.toInt(),
                            onDone: { newSeconds in
                                vm.setSeconds(newSeconds: newSeconds.toInt32())
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
                            Text(state.timerTitle)
                            Spacer()
                            Text(state.timerNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        TimerSheet(
                            title: state.timerTitle,
                            doneTitle: "Done",
                            initSeconds: state.timer.toInt(),
                            onDone: { newTimer in
                                vm.setTimer(newTimer: newTimer.toInt32())
                            }
                        )
                        .interactiveDismissDisabled()
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.finishedTextTitle)
                            Spacer()
                            FormButtonEmojiView(emoji: state.finishedText)
                        }
                    },
                    sheet: {
                        GoalFormFinishedTextSheet(
                            text: state.finishedText,
                            onDone: { newFinishedText in
                                vm.setFinishedText(newFinishedText: newFinishedText)
                            }
                        )
                    }
                )
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text("Checklists")
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
                                vm.setChecklistsDb(newChecklistsDb: newChecklistsDb)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text("Shortcuts")
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
                                vm.setShortcutsDb(newShortcutsDb: newShortcutsDb)
                            }
                        )
                    }
                )
            }
            
            if let strategy = strategy as? GoalFormStrategy.EditFormData {
                Section {
                    Button("Delete Goal") {
                        strategy.onDelete()
                        dismiss()
                    }
                    .foregroundColor(.red)
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
                    if let strategy = strategy as? GoalFormStrategy.NewFormData {
                        guard let formData: GoalFormData = state.buildFormDataOrNull(
                            dialogsManager: navigation,
                            goalDb: nil
                        ) else { return }
                        strategy.onDone(formData)
                        dismiss()
                    } else if let strategy = strategy as? GoalFormStrategy.EditFormData {
                        guard let formData: GoalFormData = state.buildFormDataOrNull(
                            dialogsManager: navigation,
                            goalDb: strategy.initGoalFormData.goalDb
                        ) else { return }
                        strategy.onDone(formData)
                        dismiss()
                    } else {
                        fatalError()
                    }
                }
                .fontWeight(.semibold)
            }
        }
    }
}
