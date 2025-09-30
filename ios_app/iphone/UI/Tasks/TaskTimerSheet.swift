import SwiftUI
import shared

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
    
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        ScrollView {
            
            VStack {
                
                ForEach(state.goalsUi, id: \.goalDb) { goalUi in
                    
                    ZStack(alignment: .bottomLeading) { // divider
                        
                        Button(
                            action: {
                                goalUi.onTap()
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
                }
            }
            .fillMaxWidth()
        }
        .defaultScrollAnchor(.bottom)
    }
}
