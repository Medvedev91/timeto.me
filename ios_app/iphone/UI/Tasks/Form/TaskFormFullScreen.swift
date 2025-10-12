import SwiftUI
import shared

extension Navigation {
    
    func showTaskForm(
        strategy: TaskFormStrategy
    ) {
        fullScreen(withAnimation: false) {
            TaskFormFullScreen(
                strategy: strategy
            )
        }
    }
}

private struct TaskFormFullScreen: View {
    
    let strategy: TaskFormStrategy
    
    var body: some View {
        VmView({
            TaskFormVm(
                strategy: strategy
            )
        }) { vm, state in
            TaskFormFullScreenInner(
                vm: vm,
                state: state,
                text: state.text,
                goalDb: state.goalDb
            )
        }
    }
}

private struct TaskFormFullScreenInner: View {
    
    let vm: TaskFormVm
    let state: TaskFormVm.State
    
    @State var text: String
    @State var goalDb: Goal2Db?
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @FocusState private var isFocused: Bool
    
    var body: some View {
        
        VStack {
            
            List {
                
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
                    .accentColor(.secondary)
                    .foregroundColor(.primary)
                    .onChange(of: goalDb) { _, newGoalDb in
                        vm.setGoal(goalDb: newGoalDb)
                    }
                    
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
            }
            .listStyle(.plain)
            
            HStack {
                
                TextField(
                    state.textPlaceholder,
                    text: $text,
                    axis: .vertical
                )
                .focused($isFocused)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .onChange(of: text) { _, newText in
                    vm.setText(text: newText)
                }
                
                Button(
                    action: {
                        vm.save(
                            dialogsManager: navigation,
                            onSuccess: {
                                dismiss()
                            }
                        )
                    },
                    label: {
                        Text(state.doneText)
                            .font(.system(size: 16, weight: .semibold))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .foregroundColor(.primary)
                            .background(roundedShape.fill(.blue))
                    }
                )
                .padding(.leading, 8)
                .padding(.trailing, H_PADDING)
            }
            .padding(.vertical, 4)
            .background(Color(.secondarySystemBackground))
        }
        .toolbar {
            
            if let strategy = vm.strategy as? TaskFormStrategy.EditTask {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Delete") {
                        vm.delete(
                            taskDb: strategy.taskDb,
                            dialogsManager: navigation,
                            onSuccess: {
                                dismiss()
                            }
                        )
                    }
                    .foregroundColor(.red)
                }
            }
            
            ToolbarItem(placement: .primaryAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
        }
        .onAppear {
            isFocused = true
        }
    }
}
