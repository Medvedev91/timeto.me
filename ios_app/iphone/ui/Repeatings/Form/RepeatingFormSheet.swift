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
                activityDb: state.activityDb
            )
        }
    }
}

private struct RepeatingFormSheetInner: View {
    
    let vm: RepeatingFormVm
    let state: RepeatingFormVm.State
    
    @State var text: String
    @State var activityDb: ActivityDb?
    
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
                            Text(state.daytimeHeader)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.daytimeNote)
                                .foregroundColor(state.daytimeUi == nil ? .red : .secondary)
                        }
                    },
                    sheet: {
                        DaytimePickerSheet(
                            title: state.daytimeHeader,
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
                
                Picker(state.activityTitle, selection: $activityDb) {
                    if activityDb == nil {
                        Text("None")
                            .tag(nil as ActivityDb?) // Support optional (nil) selection
                    }
                    ForEach(state.activitiesUi, id: \.activityDb) { activityUi in
                        Text(activityUi.title)
                            .tag(activityUi.activityDb as ActivityDb?) // Support optional (nil) selection
                    }
                }
                .pickerStyle(.menu)
                .accentColor(activityDb == nil ? .red : .secondary)
                .foregroundColor(.primary)
                .onChange(of: activityDb) { _, newActivityDb in
                    vm.setActivity(newActivityDb: newActivityDb)
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
