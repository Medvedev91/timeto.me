import SwiftUI
import shared

struct HomeTasksView: View {
    
    let tasks: [HomeVm.MainTask]
    
    private let LIST_BOTTOM_ITEM_ID = "bottom_id"
    
    var body: some View {
        
        GeometryReader { geometry in
            
            ScrollViewReader { scrollProxy in
                
                ScrollView(showsIndicators: false) {
                    
                    VStack {
                        
                        Spacer()
                        
                        ForEach(tasks.reversed(), id: \.self.taskUi.taskDb.id) { mainTask in
                            TaskItemView(mainTask: mainTask)
                        }
                        
                        ZStack {
                        }
                        .id(LIST_BOTTOM_ITEM_ID)
                    }
                    .frame(minHeight: geometry.size.height)
                }
                .frame(maxWidth: .infinity)
                .onAppear {
                    scrollProxy.scrollTo(LIST_BOTTOM_ITEM_ID)
                }
            }
        }
    }
}

///

private struct TaskItemView: View {
    
    let mainTask: HomeVm.MainTask
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    
    var body: some View {
        
        Button(
            action: {
                mainTask.taskUi.taskDb.startIntervalForUI(
                    onStarted: {},
                    activitiesSheet: {
                        nativeSheet.showActivitiesTimerSheet(
                            timerContext: mainTask.timerContext,
                            withMenu: false,
                            onStart: {}
                        )
                    },
                    timerSheet: { activity in
                        nativeSheet.showActivityTimerSheet(
                            activity: activity,
                            timerContext: mainTask.timerContext,
                            hideOnStart: true,
                            onStart: {}
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
