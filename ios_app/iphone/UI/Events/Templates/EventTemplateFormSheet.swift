import SwiftUI
import shared

struct EventTemplateFormSheet: View {
    
    let initEventTemplateDb: EventTemplateDb?
    
    var body: some View {
        VmView({
            EventTemplateFormVm(
                initEventTemplateDb: initEventTemplateDb
            )
        }) { vm, state in
            EventTemplateFormSheetInner(
                vm: vm,
                state: state,
                text: state.text,
                activityDb: state.activityDb
            )
        }
    }
}

private struct EventTemplateFormSheetInner: View {
    
    let vm: EventTemplateFormVm
    let state: EventTemplateFormVm.State
    
    @State var text: String
    @State var activityDb: ActivityDb?

    ///

    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @FocusState private var isFocused: Bool
    
    var body: some View {
        
        Form {
            
            Section {
                
                TextField(
                    state.textPlaceholder,
                    text: $text
                )
                .focused($isFocused)
                .onChange(of: text) { _, newText in
                    vm.setText(text: newText)
                }
            }
            
            Section {
                
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
                            daytimeUi: state.daytimeUiPicker,
                            onDone: { daytimeUi in
                                vm.setDaytime(daytimeUi: daytimeUi)
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
                    vm.setActivity(activityDb: newActivityDb)
                }
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.timerTitle)
                            Spacer()
                            Text(state.timerNote)
                                .foregroundColor(state.timerSeconds == nil ? .red : .secondary)
                        }
                    },
                    sheet: {
                        TimerSheet(
                            title: state.timerTitle,
                            doneTitle: "Done",
                            initSeconds: state.timerSecondsPicker.toInt(),
                            onDone: { newTimer in
                                vm.setTimer(seconds: newTimer.toInt32())
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
                                vm.setChecklists(checklistsDb: newChecklistsDb)
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
                                vm.setShortcuts(shortcutsDb: newShortcutsDb)
                            }
                        )
                    }
                )
            }
            
            if let eventTemplateDb = vm.initEventTemplateDb {
                Section {
                    Button(state.deleteText) {
                        vm.delete(
                            eventTemplateDb: eventTemplateDb,
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
