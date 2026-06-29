import SwiftUI
import shared

extension Navigation {
    
    func showTaskForm(
        strategy: TaskFormStrategy
    ) {
        fullScreen(withAnimation: false) {
            TaskFormFullScreen(
                strategy: strategy,
            )
        }
    }
}

private struct TaskFormFullScreen: View {
    
    let strategy: TaskFormStrategy
    
    var body: some View {
        VmView({
            TaskFormVm(
                strategy: strategy,
            )
        }) { vm, state in
            let state = vm.state.value as! TaskFormVm.State
            TaskFormFullScreenInner(
                vm: vm,
                state: state,
                text: state.text,
                activityDb: state.activityDb,
            )
        }
    }
}

private struct TaskFormFullScreenInner: View {
    
    let vm: TaskFormVm
    let state: TaskFormVm.State
    
    @State var text: String
    @State var activityDb: ActivityDb?
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @FocusState private var isFocused: Bool
    
    var body: some View {
        
        VStack {
            
            List {
                
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
                                initSeconds: state.timerSecondsPicker.toInt(),
                                hints: state.activityDb?.buildTimerHints().toIntList() ?? [],
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
            
            let settingsLogic = state.settingsLogic
            if let settingsLogic = settingsLogic as? TaskFormVm.SettingsLogicFixedTaskFolderUi {
                HStack {
                    
                    Image(systemName: "folder")
                        .foregroundColor(.secondary)
                        .font(.system(size: 18))
                        .padding(.leading, 14)
                        .padding(.trailing, 8)
                    
                    Text(settingsLogic.title)
                        .foregroundColor(.white)
                    
                    Spacer()
                    
                    ForEach(settingsLogic.taskFolderHintsUi, id: \.self) { taskFolderHintUi in
                        HomeTasksFolderButton(
                            taskFolderUi: taskFolderHintUi.taskFolderUi,
                            color: {
                                if settingsLogic.selectedHintUi != taskFolderHintUi  {
                                    return Color(.systemGray2)
                                }
                                if taskFolderHintUi.taskFolderUi.taskFolderDb.isToday {
                                    return .orange
                                }
                                return .indigo
                            }(),
                            onClick: {
                                vm.setSessionLogic(
                                    sessionLogic: settingsLogic.buildWithNewHint(hintUi: taskFolderHintUi),
                                )
                            },
                        )
                    }
                }
                .frame(height: HomeScreen__itemHeight)
                .padding(.trailing, 8)
            }
            else if let settingsLogic = settingsLogic as? TaskFormVm.SettingsLogicActivitiesUi {
                HStack {
                    ScrollView(.horizontal) {
                        
                        ZStack {}.frame(width: 4)
                        
                        HStack {
                            
                            ForEach(settingsLogic.activitiesUi, id: \.self) { activityUi in
                                let isSelected: Bool = state.activityDb?.id == activityUi.activityDb.id
                                ZStack {
                                    SymbolView(
                                        symbol: activityUi.symbol,
                                        color: isSelected ? .white : activityUi.colorRgba.toColor(),
                                        letterSize: 16,
                                        iconSize: 16,
                                        emojiSize: HomeScreen__itemCircleFontSize,
                                    )
                                }
                                .frame(width: HomeScreen__itemHeight, height: HomeScreen__itemHeight)
                                .background(Circle().fill(isSelected ? .blue : .black))
                                .onTapGesture {
                                    vm.setActivity(activityDb: activityUi.activityDb)
                                }
                            }
                        }
                        
                        ZStack {}.frame(width: 4)
                    }
                }
                .frame(height: HomeScreen__itemHeight)
            }

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
            .padding(.top, 8)
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
