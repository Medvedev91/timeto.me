import SwiftUI
import shared

struct ActivityFormSheet: View {
    
    let activityDb: ActivityDb?
    
    var body: some View {
        VmView({
            ActivityFormVm(
                initActivityDb: activityDb
            )
        }) { vm, state in
            ActivityFormSheetInner(
                vm: vm,
                state: state,
                name: state.name,
                keepScreenOn: state.keepScreenOn
            )
        }
    }
}

private struct ActivityFormSheetInner: View {
    
    let vm: ActivityFormVm
    let state: ActivityFormVm.State
    
    @State var name: String
    @State var keepScreenOn: Bool

    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        List {
            
            Section {
                TextField(
                    state.namePlaceholder,
                    text: $name
                )
                .onChange(of: name) { _, newName in
                    vm.setName(newName: newName)
                }
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.emojiTitle)
                            Spacer()
                            if let emoji = state.emoji {
                                Text(emoji)
                                    .font(.system(size: 27))
                            } else {
                                Text(state.emojiNotSelected)
                                    .foregroundColor(.red)
                            }
                        }
                    },
                    sheet: {
                        EmojiPickerSheet(
                            onPick: { emoji in
                                vm.setEmoji(newEmoji: emoji)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.colorTitle)
                            Spacer()
                            Circle()
                                .foregroundColor(state.colorRgba.toColor())
                                .frame(width: 31, height: 31)
                        }
                    },
                    sheet: {
                        ColorPickerSheet(
                            title: state.colorPickerTitle,
                            examplesData: state.buildColorPickerExamplesData(),
                            onPick: { colorRgba in
                                vm.setColorRgba(newColorRgba: colorRgba)
                            }
                        )
                    }
                )
            }
            
            Section {
                
                Toggle(
                    state.keepScreenOnTitle,
                    isOn: $keepScreenOn
                )
                .onChange(of: keepScreenOn) { _, new in
                    vm.setKeepScreenOn(newKeepScreenOn: new)
                }
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.pomodoroTitle)
                            Spacer()
                            Text(state.pomodoroNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ActivityFormPomodoroSheet(
                            vm: vm,
                            state: state,
                            seconds: state.pomodoroTimer.toInt32()
                        )
                    }
                )
            }
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.goalsTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.goalsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ActivityFormGoalsSheet(
                            initGoalFormsData: state.goalFormsData,
                            onPick: { newGoalFormsData in
                                vm.setGoalFormsData(newGoalFormsData: newGoalFormsData)
                            }
                        )
                    }
                )
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.timerHintsTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(state.timerHintsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        ActivityFormTimerHintsSheet(
                            timerHints: state.timerHints.toSwift(),
                            onDone: { newTimerHints in
                                vm.setTimerHints(newTimerHints: newTimerHints.toKotlin())
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
                            onPick: { newChecklistsDb in
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
                            onPick: { newShortcutsDb in
                                vm.setShortcutsDb(newShortcutsDb: newShortcutsDb)
                            }
                        )
                    }
                )
            }
            
            if let activityDb = state.activityDb {
                Section {
                    Button("Delete Activity") {
                        vm.delete(
                            activityDb: activityDb,
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
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(state.title)
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
                        onSave: {
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
            }
        }
    }
}
