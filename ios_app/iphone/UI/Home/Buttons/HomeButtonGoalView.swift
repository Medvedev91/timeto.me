import SwiftUI
import shared

struct HomeButtonGoalView: View {
    
    let goal: HomeButtonType.Goal
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        Button(
            action: {
                let isStarted = goal.onBarPressedOrNeedTimerPicker()
                if !isStarted {
                    navigation.sheet {
                        TimerSheet(
                            title: goal.timerPickerTitle,
                            doneTitle: "Start",
                            initSeconds: 45 * 60,
                            hints: goal.goalDb.buildTimerHints().toIntList(),
                            onDone: { newTimerSeconds in
                                goal.startForSeconds(seconds: newTimerSeconds.toInt32())
                            }
                        )
                    }
                }
            },
            label: {
                
                ZStack {
                    
                    ZStack {
                        
                        let goalColor = goal.bgColor.toColor()
                        
                        GeometryReader { geometry in
                            let width = geometry.size.width
                            let progressRatio: CGFloat = goal.isCompletedAsChecklist ? 1 : Double(goal.progressRatio)
                            let progressWidth: CGFloat = width * progressRatio
                            VStack {
                                ZStack {
                                }
                                .fillMaxHeight()
                                .frame(width: progressWidth)
                                .background(goalColor)
                                .animation(.easeInOut(duration: 0.150), value: progressWidth)
                                Spacer()
                            }
                        }
                        .fillMaxWidth()
                        .clipShape(roundedShape)
                        
                        HStack {
                            
                            Text(goal.leftText)
                                .padding(.leading, HomeScreen__itemCircleHPadding)
                                .foregroundColor(.white)
                                .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                            
                            Spacer()
                            
                            if goal.isCompletedAsChecklist {
                                ChecklistIconView(color: goalColor)
                            } else {
                                Text(goal.rightText)
                                    .padding(.trailing, HomeScreen__itemCircleHPadding)
                                    .foregroundColor(.white)
                                    .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                            }
                        }
                    }
                    .frame(height: HomeScreen__itemCircleHeight, alignment: .center)
                    .background(roundedShape.fill(homeFgColor))
                    .contextMenu {
                        
                        Section(goal.fullText) {
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        Goal2FormSheet(
                                            goalDb: goal.goalDb,
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
                                    navigation.sheet {
                                        TimerSheet(
                                            title: goal.timerPickerTitle,
                                            doneTitle: "Start",
                                            initSeconds: 45 * 60,
                                            hints: goal.goalDb.buildTimerHints().toIntList(),
                                            onDone: { newTimerSeconds in
                                                goal.startForSeconds(seconds: newTimerSeconds.toInt32())
                                            }
                                        )
                                    }
                                },
                                label: {
                                    Label("Timer", systemImage: "timer")
                                }
                            )
                            
                            ForEach(goal.timerHintUi, id: \.self) { timerHintUi in
                                Button(timerHintUi.title) {
                                    timerHintUi.onTap()
                                }
                            }
                            
                            ForEach(goal.childGoalsUi, id: \.self) { childGoalUi in
                                Button(childGoalUi.title) {
                                    let isStarted = childGoalUi.startOrNeedTimerPicker()
                                    if !isStarted {
                                        navigation.sheet {
                                            TimerSheet(
                                                title: childGoalUi.title,
                                                doneTitle: "Start",
                                                initSeconds: 45 * 60,
                                                hints: childGoalUi.goalDb.buildTimerHints().toIntList(),
                                                onDone: { newTimerSeconds in
                                                    childGoalUi.startForSeconds(seconds: newTimerSeconds.toInt32())
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                            
                            if !goal.childGoalsUi.isEmpty || !goal.timerHintUi.isEmpty {
                                Divider()
                            }
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        DaytimePickerSheet(
                                            title: "Until Time",
                                            doneText: "Start",
                                            daytimeUi: DaytimeUi.companion.now(),
                                            onDone: { daytimePickerUi in
                                                daytimePickerUi.startUntilAsync(goalDb: goal.goalDb)
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
                                role: goal.restOfGoalSeconds <= 0 ? .destructive : .none,
                                action: {
                                    goal.startRestOfGoal()
                                },
                                label: {
                                    Label(goal.restOfGoalTitle, systemImage: "flag.pattern.checkered")
                                }
                            )
                        }
                        
                        Section {
                            
                            Button(
                                action: {
                                    navigation.fullScreen {
                                        HomeSettingsButtonsFullScreen(
                                            onClose: {}
                                        )
                                    }
                                },
                                label: {
                                    Label("Home Screen Settings", systemImage: "gear")
                                }
                            )
                        }
                    }
                }
                .frame(height: HomeScreen__itemHeight, alignment: .center)
            }
        )
    }
}

private struct ChecklistIconView: View {
    
    let color: Color
    
    var body: some View {
        ZStack {
            Image(systemName: "checkmark")
                .foregroundColor(color)
                .font(.system(size: 10, weight: .bold))
        }
        .frame(width: HomeScreen__itemCircleHeight - 6, height: HomeScreen__itemCircleHeight - 6)
        .background(Circle().fill(.white))
        .padding(.trailing, 3)
    }
}
