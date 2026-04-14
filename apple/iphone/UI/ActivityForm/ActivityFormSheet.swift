import SwiftUI
import shared

struct ActivityFormSheet: View {
    
    let activityDb: ActivityDb?
    let onSave: (ActivityDb) -> Void
    
    var body: some View {
        VmView({
            ActivityFormVm(
                initActivityDb: activityDb
            )
        }) { vm, state in
            ActivityFormSheetInner(
                vm: vm,
                state: state,
                onSave: onSave,
                name: state.name,
                seconds: state.seconds,
                secondsNote: state.secondsNote,
                parentActivityUi: state.parentActivityUi,
                isDoneEnabled: state.isDoneEnabled,
                timerTypeId: state.timerTypeId,
                showFixedTimerPicker: state.showFixedTimerPicker,
                showDaytimeTimerPicker: state.showDaytimeTimerPicker,
                keepScreenOn: state.keepScreenOn,
                pomodoroTimer: state.pomodoroTimer,
            )
        }
    }
}

private struct ActivityFormSheetInner: View {
    
    let vm: ActivityFormVm
    let state: ActivityFormVm.State
    
    let onSave: (ActivityDb) -> Void
    
    @State var name: String
    @State var seconds: Int32
    @State var secondsNote: String
    @State var parentActivityUi: ActivityFormVm.ActivityUi?
    @State var isDoneEnabled: Bool
    @State var timerTypeId: ActivityFormVm.TimerTypeItemUiTimerTypeUiId
    @State var showFixedTimerPicker: Bool
    @State var showDaytimeTimerPicker: Bool
    @State var keepScreenOn: Bool
    @State var pomodoroTimer: Int32
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @FocusState private var focusedField: FocusedField?
    
    @State private var isSecondsPickerExpanded = false
    private var secondsNoteColor: Color {
        isSecondsPickerExpanded ? .pink : .secondary
    }
    
    var body: some View {
        List {
            
            Section {
                
                TextField(
                    state.namePlaceholder,
                    text: $name
                )
                .focused($focusedField, equals: .name)
                .onChange(of: name) { _, newName in
                    vm.setName(newName: newName)
                }
                
                Button(
                    action: {
                        withAnimation {
                            isSecondsPickerExpanded.toggle()
                        }
                    },
                    label: {
                        HStack {
                            Text(state.secondsTitle)
                                .foregroundColor(.primary)
                            Spacer()
                            Text(secondsNote)
                                .foregroundColor(secondsNoteColor)
                            Image(systemName: "chevron.up.chevron.down")
                                .padding(.leading, 4)
                                .font(.system(size: 13, weight: .regular))
                                .foregroundColor(secondsNoteColor)
                        }
                    },
                )
                .animateVmValue(vmValue: state.secondsNote, swiftState: $secondsNote)
                
                if isSecondsPickerExpanded {
                    Picker("", selection: $seconds) {
                        ForEach(state.secondsPickerItemsUi, id: \.seconds) { itemUi in
                            Text(itemUi.title)
                        }
                    }
                    .pickerStyle(.wheel)
                    .onChange(of: seconds) { _, newSeconds in
                        vm.setSeconds(newSeconds: newSeconds)
                    }
                }
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
            
            Section {
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text(state.periodTitle)
                            Spacer()
                            Text(state.periodNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        GoalFormPeriodSheet(
                            initActivityDbPeriod: state.period,
                            onDone: { newPeriod in
                                vm.setPeriod(newPeriod: newPeriod)
                            }
                        )
                    }
                )
            }
            
            Section {
                
                Picker(state.timerTypeTitle, selection: $timerTypeId) {
                    ForEach(state.timerTypeItemsUi, id: \.id) { timerTypeItemUi in
                        Text(timerTypeItemUi.title)
                            .tag(timerTypeItemUi.id)
                    }
                }
                .onChange(of: timerTypeId) { _, newTimerTypeId in
                    vm.setTimerTypeId(newTimerTypeId: newTimerTypeId)
                }
                .animateVmValue(vmValue: state.showFixedTimerPicker, swiftState: $showFixedTimerPicker)
                .animateVmValue(vmValue: state.showDaytimeTimerPicker, swiftState: $showDaytimeTimerPicker)
                
                if showFixedTimerPicker {
                    NavigationLinkSheet(
                        label: {
                            HStack {
                                Text(state.fixedTimerTitle)
                                Spacer()
                                Text(state.fixedTimerNote)
                                    .foregroundColor(.secondary)
                            }
                        },
                        sheet: {
                            TimerSheet(
                                title: state.fixedTimerTitle,
                                doneTitle: "Done",
                                initSeconds: state.fixedTimer.toInt(),
                                hints: [],
                                onDone: { newTimer in
                                    vm.setFixedTimer(newFixedTimer: newTimer.toInt32())
                                }
                            )
                            .interactiveDismissDisabled()
                        }
                    )
                }
                
                if showDaytimeTimerPicker {
                    NavigationLinkSheet(
                        label: {
                            HStack {
                                Text(state.daytimeTimerTitle)
                                Spacer()
                                Text(state.daytimeTimerNote)
                                    .foregroundColor(.secondary)
                            }
                        },
                        sheet: {
                            DaytimePickerSheet(
                                title: state.daytimeTimerTitle,
                                doneText: "Done",
                                daytimeUi: state.timerDaytimeUi,
                                onDone: { daytimeUi in
                                    vm.setDaytimeTimer(newDaytimeUi: daytimeUi)
                                },
                                onRemove: nil,
                            )
                            .interactiveDismissDisabled()
                        }
                    )
                }
            }
            
            Section {
                
                Picker(state.parentActivityTitle, selection: $parentActivityUi) {
                    Text("None")
                        .tag(nil as ActivityFormVm.ActivityUi?) // Support optional (nil) selection
                    ForEach(state.parentActivitiesUi, id: \.activityDb.id) { activityUi in
                        Text(activityUi.title)
                            .tag(activityUi as ActivityFormVm.ActivityUi?) // Support optional (nil) selection
                    }
                }
                .onChange(of: parentActivityUi) { _, newParentActivityUi in
                    vm.setParentActivityUi(activityUi: newParentActivityUi)
                }
            }
            
            Section {
                
                NavigationLinkAction(
                    label: {
                        HStack {
                            Text(state.colorTitle)
                            Spacer()
                            Circle()
                                .foregroundColor(state.colorRgba.toColor())
                                .frame(width: 31, height: 31)
                        }
                    },
                    action: {
                        focusedField = nil
                        navigation.fullScreen {
                            ColorPickerSheet(
                                title: state.colorPickerTitle,
                                examplesUi: state.buildColorPickerExamplesUi(),
                                onDone: { colorRgba in
                                    vm.setColorRgba(newColorRgba: colorRgba)
                                }
                            )
                        }
                    }
                )
            }
            
            Section {
                
                Picker(state.pomodoroTitle, selection: $pomodoroTimer) {
                    ForEach(state.pomodoroItemsUi, id: \.timer) { itemUi in
                        Text(itemUi.title)
                            .tag(itemUi.timer)
                    }
                }
                .onChange(of: pomodoroTimer) { _, newPomodoroTimer in
                    vm.setPomodoroTimer(newPomodoroTimer: newPomodoroTimer)
                }
                
                NavigationLinkSheet(
                    label: {
                        HStack {
                            Text("Timer Hints")
                            Spacer()
                            Text(state.timerHintsNote)
                                .foregroundColor(.secondary)
                        }
                    },
                    sheet: {
                        GoalFormTimerHintsSheet(
                            initTimerHints: state.timerHints.map { $0.toInt() },
                            onDone: { newTimerHints in
                                vm.setTimerHints(newTimerHints: newTimerHints.map { $0.toKotlinInt() })
                            },
                        )
                    },
                )
                
                Toggle(
                    state.keepScreenOnTitle,
                    isOn: $keepScreenOn
                )
                .onChange(of: keepScreenOn) { _, new in
                    vm.setKeepScreenOn(newKeepScreenOn: new)
                }
            }
            
            if let activityDb = state.initActivityDb {
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
        .ignoresSafeArea(.keyboard)
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
                    focusedField = nil
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: { newActivityDb in
                            onSave(newActivityDb)
                            dismiss()
                        }
                    )
                }
                .fontWeight(.semibold)
                .disabled(!isDoneEnabled)
                .animateVmValue(vmValue: state.isDoneEnabled, swiftState: $isDoneEnabled)
            }
        }
        // Hide keyboard on scroll
        .simultaneousGesture(DragGesture().onChanged({ _ in
            focusedField = nil
        }))
        .onAppear {
            if state.initActivityDb == nil {
                focusedField = .name
            }
        }
        .onChange(of: isSecondsPickerExpanded) { _, newValue in
            if newValue {
                focusedField = nil
            }
        }
    }
}

private enum FocusedField {
    case name
}
