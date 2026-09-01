import SwiftUI
import shared

struct WidgetFullButtonsActivityView: View {
    
    let activity: HomeButtonType.Activity
    
    ///
    
    var body: some View {
        
        Link(destination: WidgetLink.ActivityButton(activityId: activity.activityDb.id.toInt()).buildUrl()) {
            
            ZStack {
                
                ZStack {
                    
                    GeometryReader { geometry in
                        let width = geometry.size.width
                        let progressRatio: CGFloat = Double(activity.progressRatio)
                        let progressWidth: CGFloat = width * progressRatio
                        VStack {
                            ZStack {
                            }
                            .fillMaxHeight()
                            .frame(width: progressWidth)
                            .background(activity.bgColor.toColor())
                        }
                    }
                    .fillMaxWidth()
                    .clipShape(roundedShape)
                    
                    if activity.sort.size == 1 {
                        ZStack {
                            
                            HStack {
                                // todo calc once
                                let symbol = activity.activityDb.symbolOrDefault()
                                
                                SymbolView(
                                    symbol: symbol,
                                    color: .white,
                                    letterSize: widgetFullItemCircleFontSize,
                                    iconSize: 11,
                                    emojiSize: widgetFullItemCircleFontSize,
                                )
                                .padding(.leading, symbol is Symbol.Emoji ? 4 : 6)
                                Spacer()
                            }
                            
                            HStack {
                                Spacer()
                                RightBarView(
                                    activity: activity,
                                )
                            }
                        }
                        .fillMaxSize()
                    }
                    else {
                        HStack {
                            
                            Text(activity.leftText)
                                .padding(.leading, widgetFullItemCircleHPadding)
                                .foregroundColor(.white)
                                .font(.system(size: widgetFullItemCircleFontSize, weight: widgetFullItemCircleFontWeight))
                            
                            Spacer()
                            
                            RightBarView(
                                activity: activity,
                            )
                        }
                    }
                }
                .frame(height: widgetFullItemCircleHeight, alignment: .center)
                .background(roundedShape.fill(widgetFgColor))
            }
            .frame(height: widgetFullItemHeight, alignment: .center)
        }
    }
}

private struct RightBarView: View {
    
    let activity: HomeButtonType.Activity
    
    var body: some View {
        
        if activity.isCompleted {
            ZStack {
                Image(systemName: "checkmark")
                    .foregroundColor(activity.bgColor.toColor())
                    .font(.system(size: 10, weight: .bold))
            }
            .frame(width: widgetFullItemCircleHeight - 6, height: widgetFullItemCircleHeight - 6)
            .background(Circle().fill(.white))
            .padding(.trailing, 3)
        } else {
            if activity.isActive,
               activity.goalType is ActivityDb.GoalTypeTimer,
               let timer = activity.barsActivityStats.calcRestOfGoalTfTimerType() as? TextFeatures.TimerTypeTimer {
                let date: Date = Date().inSeconds(timer.seconds.toInt())
                Text(
                    timerInterval: date.widgetTimerRange(isTimerOrStopwatch: true),
                    countsDown: true,
                )
                .textAlign(.trailing)
                .padding(.trailing, widgetFullItemCircleHPadding)
                .foregroundColor(.white)
                .font(.system(size: widgetFullItemCircleFontSize, weight: widgetFullItemCircleFontWeight))
            } else {
                Text(activity.rightText)
                    .padding(.trailing, widgetFullItemCircleHPadding)
                    .foregroundColor(.white)
                    .font(.system(size: widgetFullItemCircleFontSize, weight: widgetFullItemCircleFontWeight))
            }
        }
    }
}
