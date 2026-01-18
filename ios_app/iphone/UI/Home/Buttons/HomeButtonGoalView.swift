import SwiftUI
import shared

struct HomeButtonGoalView: View {
    
    let goal: HomeButtonType.Goal
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        Button(
            action: {
                goal.startInterval()
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
                                            title: goal.goalTf.textNoFeatures,
                                            doneTitle: "Start",
                                            initSeconds: 45 * 60,
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
