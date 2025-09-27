import SwiftUI
import shared

struct Goal2FormSheet: View {
    
    let goalDb: Goal2Db?
    
    var body: some View {
        VmView({
            Goal2FormVm(
                initGoalDb: goalDb
            )
        }) { vm, state in
            Goal2FormSheetInner(
                vm: vm,
                state: state,
                name: state.name,
                seconds: state.seconds,
                secondsNote: state.secondsNote,
                parentGoalUi: state.parentGoalUi,
                isDoneEnabled: state.isDoneEnabled,
                isTimerRestOfBar: state.timer == 0,
                keepScreenOn: state.keepScreenOn,
                pomodoroTimer: state.pomodoroTimer,
            )
        }
    }
}

private struct Goal2FormSheetInner: View {
    
    let vm: Goal2FormVm
    let state: Goal2FormVm.State
    
    @State var name: String
    @State var seconds: Int32
    @State var secondsNote: String
    @State var parentGoalUi: Goal2FormVm.GoalUi?
    @State var isDoneEnabled: Bool
    @State var isTimerRestOfBar: Bool
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
                            initGoalDbPeriod: state.period,
                            onDone: { newPeriod in
                                vm.setPeriod(newPeriod: newPeriod)
                            }
                        )
                    }
                )
            }
            
            Section(state.timerHeader) {
                
                Toggle(
                    state.timerTitleRest,
                    isOn: $isTimerRestOfBar
                )
                .onChange(of: state.timer) { _, new in
                    withAnimation {
                        isTimerRestOfBar = (new == 0)
                    }
                }
                .onChange(of: isTimerRestOfBar) { _, new in
                    vm.setTimer(newTimer: new ? 0 : (45 * 60))
                }
                
                if !isTimerRestOfBar {
                    NavigationLinkSheet(
                        label: {
                            HStack {
                                Text(state.timerTitleTimer)
                                Spacer()
                                Text(state.timerNote)
                                    .foregroundColor(.secondary)
                            }
                        },
                        sheet: {
                            TimerSheet(
                                title: state.timerTitleTimer,
                                doneTitle: "Done",
                                initSeconds: state.timer.toInt(),
                                onDone: { newTimer in
                                    vm.setTimer(newTimer: newTimer.toInt32())
                                }
                            )
                            .interactiveDismissDisabled()
                        }
                    )
                }
            }
            
            Section {
                
                Picker(state.parentGoalTitle, selection: $parentGoalUi) {
                    Text("None")
                        .tag(nil as Goal2FormVm.GoalUi?) // Support optional (nil) selection
                    ForEach(state.parentGoalsUi, id: \.goalDb.id) { goalUi in
                        Text(goalUi.title)
                            .tag(goalUi as Goal2FormVm.GoalUi?) // Support optional (nil) selection
                    }
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
                
                Toggle(
                    state.keepScreenOnTitle,
                    isOn: $keepScreenOn
                )
                .onChange(of: keepScreenOn) { _, new in
                    vm.setKeepScreenOn(newKeepScreenOn: new)
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
                    vm.save(
                        dialogsManager: navigation,
                        onSuccess: {
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
            focusedField = .name
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
