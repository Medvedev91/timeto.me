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
                            
                            Text(goal.textLeft)
                                .padding(.leading, HomeScreen__itemCircleHPadding)
                                .foregroundColor(.white)
                                .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                            
                            Spacer()
                            
                            Text(goal.textRight)
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
                                        GoalFormSheet(
                                            strategy: GoalFormStrategy.EditGoal(
                                                goalDb: goal.goalDb
                                            )
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
                                    navigation.fullScreen {
                                        HomeSettingsButtonsFullScreen()
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
