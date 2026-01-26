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
                        
                        GeometryReader { geometry in
                            VStack {
                                ZStack {
                                }
                                .fillMaxHeight()
                                .frame(width: geometry.size.width * Double(goal.progressRatio))
                                .background(goal.bgColor.toColor())
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
                            
                            Text(goal.rightText)
                                .padding(.trailing, HomeScreen__itemCircleHPadding)
                                .foregroundColor(.white)
                                .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
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
