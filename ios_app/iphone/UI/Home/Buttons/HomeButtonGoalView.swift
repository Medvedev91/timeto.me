import SwiftUI
import shared

struct HomeButtonGoalView: View {
    
    let goal: HomeButtonType.Goal
    
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
                }
                .frame(height: HomeScreen__itemHeight, alignment: .center)
            }
        )
    }
}
