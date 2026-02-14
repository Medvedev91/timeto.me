import SwiftUI
import shared

// todo Go to Home Screen on start.

private let listItemHeight: CGFloat = 46

extension Navigation {
    
    func showTaskTimerSheet(taskDb: TaskDb) {
        self.sheet {
            VmView({
                TaskTimerVm(
                    taskDb: taskDb
                )
            }) { vm, state in
                TaskTimerSheetInner(
                    vm: vm,
                    state: state,
                )
                .presentationDetents([.height(listItemHeight * CGFloat(state.goalsUi.count))])
                .presentationDragIndicator(.visible)
            }
        }
    }
}

///

private struct TaskTimerSheetInner: View {
    
    let vm: TaskTimerVm
    let state: TaskTimerVm.State

    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        ScrollView {
            
            VStack {
                
                ForEach(state.goalsUi, id: \.goalDb) { goalUi in
                    
                    ZStack(alignment: .bottomLeading) { // divider
                        
                        Button(
                            action: {
                                showTimerSheet(goalUi: goalUi)
                            },
                            label: {
                                
                                HStack {
                                    
                                    Text(goalUi.text)
                                        .foregroundColor(.primary)
                                        .truncationMode(.tail)
                                        .lineLimit(1)
                                        .padding(.leading, H_PADDING)
                                    
                                    Spacer()
                                    
                                    let timerHintsUi: [TaskTimerVm.TimerHintUi] = goalUi.timerHintsUi
                                    ForEach(timerHintsUi, id: \.seconds) { timerHintUi in
                                        Button(
                                            action: {
                                                dismiss()
                                                timerHintUi.onTap()
                                            },
                                            label: {
                                                Text(timerHintUi.title)
                                                    .foregroundColor(.blue)
                                                    .padding(.horizontal, 5)
                                                    .padding(.vertical, 4)
                                            }
                                        )
                                        .buttonStyle(.borderless)
                                    }
                                }
                                .padding(.trailing, 8)
                                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                            }
                        )

                        if state.goalsUi.last != goalUi {
                            Divider()
                                .padding(.leading, H_PADDING)
                        }
                    }
                    .frame(height: listItemHeight)
                    .contextMenu {
                        
                        Section {
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        Goal2FormSheet(
                                            goalDb: goalUi.goalDb,
                                            onSave: { _ in }
                                        )
                                    }
                                },
                                label: {
                                    Label("Edit", systemImage: "square.and.pencil")
                                }
                            )
                        }

                        Section {
                            
                            Button(
                                action: {
                                    showTimerSheet(goalUi: goalUi)
                                },
                                label: {
                                    Label("Timer", systemImage: "timer")
                                }
                            )
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        DaytimePickerSheet(
                                            title: "Until Time",
                                            doneText: "Start",
                                            daytimeUi: DaytimeUi.companion.now(),
                                            onDone: { daytimePickerUi in
                                                goalUi.startUntil(daytimeUi: daytimePickerUi)
                                                dismiss()
                                            },
                                            onRemove: {}
                                        )
                                        .presentationDetents([.medium])
                                        .presentationDragIndicator(.visible)
                                    }
                                },
                                label: {
                                    Label("Until Time", systemImage: "clock")
                                }
                            )
                            
                            Button(
                                action: {
                                    goalUi.startRestOfGoal()
                                    dismiss()
                                },
                                label: {
                                    Label("Rest of Goal", systemImage: "flag.pattern.checkered")
                                }
                            )
                        }
                    }
                }
            }
            .fillMaxWidth()
        }
        .defaultScrollAnchor(.bottom)
    }
    
    private func showTimerSheet(goalUi: TaskTimerVm.GoalUi) {
        navigation.sheet {
            TimerSheet(
                title: goalUi.text,
                doneTitle: "Start",
                initSeconds: 45 * 60,
                hints: goalUi.goalDb.buildTimerHints().toIntList(),
                onDone: { newTimerSeconds in
                    goalUi.start(timer: newTimerSeconds.toInt32())
                    dismiss()
                }
            )
        }
    }
}
