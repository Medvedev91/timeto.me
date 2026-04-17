import SwiftUI
import shared

private let progressAnimation: Animation = .spring

struct HomeButtonActivityView: View {
    
    let activity: HomeButtonType.Activity
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        Button(
            action: {
                let isStarted = activity.onBarPressedOrNeedTimerPicker()
                if !isStarted {
                    navigation.sheet {
                        TimerSheet(
                            title: activity.timerPickerTitle,
                            doneTitle: "Start",
                            initSeconds: 45 * 60,
                            hints: activity.activityDb.buildTimerHints().toIntList(),
                            onDone: { newTimerSeconds in
                                activity.startForSeconds(seconds: newTimerSeconds.toInt32())
                            }
                        )
                    }
                }
            },
            label: {
                
                ZStack {
                    
                    ZStack {
                        
                        let activityColor = activity.bgColor.toColor()
                        
                        GeometryReader { geometry in
                            let width = geometry.size.width
                            let progressRatio: CGFloat = activity.isCompletedAsChecklist ? 1 : Double(activity.progressRatio)
                            let progressWidth: CGFloat = width * progressRatio
                            VStack {
                                ZStack {
                                }
                                .fillMaxHeight()
                                .frame(width: progressWidth)
                                .background(activityColor)
                                .animation(progressAnimation, value: progressWidth)
                            }
                        }
                        .fillMaxWidth()
                        .clipShape(roundedShape)
                        
                        HStack {
                            
                            Text(activity.leftText)
                                .padding(.leading, HomeScreen__itemCircleHPadding)
                                .foregroundColor(.white)
                                .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                            
                            Spacer()
                            
                            if activity.isCompletedAsChecklist {
                                ChecklistIconView(color: activityColor)
                            } else {
                                Text(activity.rightText)
                                    .padding(.trailing, HomeScreen__itemCircleHPadding)
                                    .foregroundColor(.white)
                                    .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                            }
                        }
                    }
                    .frame(height: HomeScreen__itemCircleHeight, alignment: .center)
                    .background(roundedShape.fill(homeFgColor))
                    .contextMenu {
                        
                        Section(activity.fullText) {
                            
                            Button(
                                action: {
                                    navigation.sheet {
                                        ActivityFormSheet(
                                            activityDb: activity.activityDb,
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
                                            title: activity.timerPickerTitle,
                                            doneTitle: "Start",
                                            initSeconds: 45 * 60,
                                            hints: activity.activityDb.buildTimerHints().toIntList(),
                                            onDone: { newTimerSeconds in
                                                activity.startForSeconds(seconds: newTimerSeconds.toInt32())
                                            },
                                        )
                                    }
                                },
                                label: {
                                    Label("Timer", systemImage: "timer")
                                }
                            )
                            
                            ForEach(activity.timerHintUi, id: \.self) { timerHintUi in
                                Button(timerHintUi.title) {
                                    timerHintUi.onTap()
                                }
                            }
                            
                            ForEach(activity.childActivitiesUi, id: \.self) { childActivityUi in
                                Button(childActivityUi.title) {
                                    let isStarted = childActivityUi.startOrNeedTimerPicker()
                                    if !isStarted {
                                        navigation.sheet {
                                            TimerSheet(
                                                title: childActivityUi.title,
                                                doneTitle: "Start",
                                                initSeconds: 45 * 60,
                                                hints: childActivityUi.activityDb.buildTimerHints().toIntList(),
                                                onDone: { newTimerSeconds in
                                                    childActivityUi.startForSeconds(seconds: newTimerSeconds.toInt32())
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                            
                            if !activity.childActivitiesUi.isEmpty || !activity.timerHintUi.isEmpty {
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
                                                daytimePickerUi.startUntilAsync(activityDb: activity.activityDb)
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
                            
                            if let restOfGoalUi = activity.restOfGoalUi {
                                Button(
                                    action: {
                                        activity.startRestOfGoal()
                                    },
                                    label: {
                                        Label(restOfGoalUi.title, systemImage: "flag.pattern.checkered")
                                    }
                                )
                            }
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
