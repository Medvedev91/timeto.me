import SwiftUI
import shared

let HomeScreen__ITEM_HEIGHT: CGFloat = 38
let HomeScreen__PRIMARY_FONT_SIZE: CGFloat = 18

struct HomeScreen: View {
    
    var body: some View {
        VmView({
            HomeVm()
        }) { vm, state in
            HomeScreenInner(
                vm: vm,
                state: state
            )
        }
    }
}

///

private let itemCircleHPadding: CGFloat = 7
private let itemCircleHeight: CGFloat = 24
private let itemCircleFontSize: CGFloat = 15
private let itemCircleFontWeight: Font.Weight = .semibold

private struct HomeScreenInner: View {
    
    let vm: HomeVm
    let state: HomeVm.State
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        VStack {
            
            let checklistDb: ChecklistDb? = state.checklistDb
            
            HomeTimerView(vm: vm, state: state)
            
            if let readmeMessage = state.readmeMessage {
                Button(
                    action: {
                        vm.onReadmeOpen()
                        navigation.fullScreen {
                            ReadmeFullScreen(defaultItem: .basics)
                        }
                    },
                    label: {
                        Text(readmeMessage)
                            .foregroundColor(c.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .font(.system(size: 17, weight: .medium))
                            .background(roundedShape.fill(.red))
                            .padding(.top, 8)
                    }
                )
            }
            
            if let whatsNewMessage = state.whatsNewMessage {
                NavigationLink(.whatsNew) {
                    Text(whatsNewMessage)
                        .foregroundColor(.white)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .font(.system(size: 17, weight: .medium))
                        .background(roundedShape.fill(.red))
                        .padding(.top, 8)
                }
            }
            
            let isMainTasksExists = !state.mainTasks.isEmpty
            
            GeometryReader { geometry in
                
                let _ = vm.upListsContainerSize(
                    totalHeight: Float(geometry.size.height),
                    itemHeight: Float(HomeScreen__ITEM_HEIGHT)
                )
                
                VStack {
                    
                    if let checklistDb = checklistDb {
                        VStack {
                            ChecklistView(
                                checklistDb: checklistDb,
                                maxLines: 1,
                                onDelete: {}
                            )
                        }
                        .frame(height: CGFloat(state.listsSizes.checklist))
                        .id("home_checklist_id_\(checklistDb.id)") // Force update on change
                    }
                    
                    if isMainTasksExists {
                        MainTasksView(
                            tasks: state.mainTasks
                        )
                        .frame(height: CGFloat(state.listsSizes.mainTasks))
                    }
                    
                    Spacer()
                }
            }
            
            ForEachIndexed(
                state.goalBarsUi,
                content: { idx, goalBarUi in
                    
                    Button(
                        action: {
                            goalBarUi.startInterval()
                        },
                        label: {
                            
                            ZStack {
                                
                                ZStack {
                                    
                                    GeometryReader { geometry in
                                        VStack {
                                            ZStack {
                                            }
                                            .frame(maxHeight: .infinity)
                                            .frame(width: geometry.size.width * Double(goalBarUi.ratio))
                                            .background(goalBarUi.bgColor.toColor())
                                            Spacer()
                                        }
                                    }
                                    .fillMaxWidth()
                                    .clipShape(roundedShape)
                                    
                                    HStack {
                                        
                                        Text(goalBarUi.textLeft)
                                            .padding(.leading, itemCircleHPadding)
                                            .foregroundColor(c.white)
                                            .font(.system(size: itemCircleFontSize, weight: itemCircleFontWeight))
                                        
                                        Spacer()
                                        
                                        Text(goalBarUi.textRight)
                                            .padding(.trailing, itemCircleHPadding)
                                            .foregroundColor(c.white)
                                            .font(.system(size: itemCircleFontSize, weight: itemCircleFontWeight))
                                    }
                                }
                                .frame(height: itemCircleHeight, alignment: .center)
                                .background(roundedShape.fill(c.homeFg))
                                .padding(.horizontal, H_PADDING)
                            }
                            .frame(height: HomeScreen__ITEM_HEIGHT, alignment: .center)
                            .offset(y: 1)
                        }
                    )
                }
            )
            
            Padding(vertical: 10.0)
        }
        .padding(.bottom, MainTabsView__HEIGHT)
    }
}

///

private struct MainTasksView: View {
    
    let tasks: [HomeVm.MainTask]
    
    private let LIST_BOTTOM_ITEM_ID = "bottom_id"
    
    var body: some View {
        
        GeometryReader { geometry in
            
            ScrollViewReader { scrollProxy in
                
                ScrollView(showsIndicators: false) {
                    
                    VStack {
                        
                        Spacer()
                        
                        ForEach(tasks.reversed(), id: \.self.taskUi.taskDb.id) { mainTask in
                            MainTaskItemView(mainTask: mainTask)
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

private struct MainTaskItemView: View {
    
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
                            .font(.system(size: itemCircleFontSize, weight: itemCircleFontWeight))
                            .padding(.horizontal, itemCircleHPadding)
                            .frame(height: itemCircleHeight)
                            .background(roundedShape.fill(timeUI.textBgColor.toColor()))
                            .padding(.trailing, mainTask.taskUi.tf.paused != nil ? 9 : 8)
                    }
                    
                    if mainTask.taskUi.tf.paused != nil {
                        ZStack {
                            Image(systemName: "pause")
                                .foregroundColor(c.white)
                                .font(.system(size: 12, weight: .black))
                        }
                        .frame(width: itemCircleHeight, height: itemCircleHeight)
                        .background(roundedShape.fill(c.green))
                        .padding(.trailing, 8)
                    }
                    
                    Text(mainTask.text)
                        .font(.system(size: HomeScreen__PRIMARY_FONT_SIZE))
                        .foregroundColor(Color.white)
                        .padding(.trailing, 4)
                    
                    Spacer()
                    
                    if let timeUI = mainTask.timeUI {
                        Text(timeUI.note)
                            .foregroundColor(timeUI.noteColor.toColor())
                            .font(.system(size: HomeScreen__PRIMARY_FONT_SIZE))
                    }
                }
                .frame(height: HomeScreen__ITEM_HEIGHT)
                .padding(.horizontal, H_PADDING)
            }
        )
    }
}
