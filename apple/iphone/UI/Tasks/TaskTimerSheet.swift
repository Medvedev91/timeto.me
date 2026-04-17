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
                .presentationDetents([.height(listItemHeight * CGFloat(state.activitiesUi.count))])
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
                
                ForEach(state.activitiesUi, id: \.activityDb) { activityUi in
                    
                    ZStack(alignment: .bottomLeading) { // divider
                        
                        Button(
                            action: {
                                showTimerSheet(activityUi: activityUi)
                            },
                            label: {
                                
                                HStack {
                                    
                                    Text(activityUi.text)
                                        .foregroundColor(.primary)
                                        .truncationMode(.tail)
                                        .lineLimit(1)
                                        .padding(.leading, H_PADDING)
                                    
                                    Spacer()
                                    
                                    let timerHintsUi: [TaskTimerVm.TimerHintUi] = activityUi.timerHintsUi
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

                        if state.activitiesUi.last != activityUi {
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
                                        ActivityFormSheet(
                                            activityDb: activityUi.activityDb,
                                            onSave: { _ in },
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
                                    showTimerSheet(activityUi: activityUi)
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
                                                activityUi.startUntil(daytimeUi: daytimePickerUi)
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
                        }
                    }
                }
            }
            .fillMaxWidth()
        }
        .defaultScrollAnchor(.bottom)
    }
    
    private func showTimerSheet(activityUi: TaskTimerVm.ActivityUi) {
        navigation.sheet {
            TimerSheet(
                title: activityUi.text,
                doneTitle: "Start",
                initSeconds: 45 * 60,
                hints: activityUi.activityDb.buildTimerHints().toIntList(),
                onDone: { newTimerSeconds in
                    activityUi.start(timer: newTimerSeconds.toInt32())
                    dismiss()
                }
            )
        }
    }
}
