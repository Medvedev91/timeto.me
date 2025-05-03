import SwiftUI
import shared

struct HomeTasksView: View {
    
    let tasks: [HomeVm.MainTask]
    
    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack {
                ForEach(tasks.reversed(), id: \.self.taskUi.taskDb.id) { mainTask in
                    TaskItemView(mainTask: mainTask)
                }
            }
        }
        .defaultScrollAnchor(.bottom)
    }
}

///

private struct TaskItemView: View {
    
    let mainTask: HomeVm.MainTask
    
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        Button(
            action: {
                mainTask.taskUi.taskDb.startIntervalForUi(
                    ifJustStarted: {},
                    ifActivityNeeded: {
                        navigation.showActivitiesTimerSheet(
                            strategy: mainTask.timerStrategy
                        )
                    },
                    ifTimerNeeded: { activityDb in
                        navigation.showActivityTimerSheet(
                            activityDb: activityDb,
                            strategy: mainTask.timerStrategy,
                            hideOnStart: true
                        )
                    }
                )
            },
            label: {
                
                HStack {
                    
                    if let timeUI = mainTask.timeUI {
                        Text(timeUI.text)
                            .foregroundColor(.white)
                            .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                            .padding(.horizontal, HomeScreen__itemCircleHPadding)
                            .frame(height: HomeScreen__itemCircleHeight)
                            .background(roundedShape.fill(timeUI.textBgColor.toColor()))
                            .padding(.trailing, mainTask.taskUi.tf.paused != nil ? 9 : 8)
                    }
                    
                    if mainTask.taskUi.tf.paused != nil {
                        ZStack {
                            Image(systemName: "pause")
                                .foregroundColor(c.white)
                                .font(.system(size: 12, weight: .black))
                        }
                        .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
                        .background(roundedShape.fill(c.green))
                        .padding(.trailing, 8)
                    }
                    
                    Text(mainTask.text)
                        .font(.system(size: HomeScreen__primaryFontSize))
                        .foregroundColor(Color.white)
                        .padding(.trailing, 4)
                    
                    Spacer()
                    
                    if let timeUI = mainTask.timeUI {
                        Text(timeUI.note)
                            .foregroundColor(timeUI.noteColor.toColor())
                            .font(.system(size: HomeScreen__primaryFontSize))
                    }
                }
                .frame(height: HomeScreen__itemHeight)
                .padding(.horizontal, H_PADDING)
            }
        )
    }
}
