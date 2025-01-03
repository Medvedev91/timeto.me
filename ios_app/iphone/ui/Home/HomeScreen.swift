import SwiftUI
import Combine
import shared

let HomeView__BOTTOM_NAVIGATION_HEIGHT = 56.0
let HomeView__PRIMARY_FONT_SIZE = 18.0

// MTG - Main Tasks & Goals
let HomeView__MTG_ITEM_HEIGHT = 38.0
private let mtgCircleHPadding = 7.0
private let mtgCircleHeight = 24.0
private let mtgCircleFontSize = 15.0
private let mtgCircleFontWeight: Font.Weight = .semibold

struct HomeScreen: View {
    
    @State private var vm = HomeVm()
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    @EnvironmentObject private var navigation: Navigation
    
    @State private var triggersChecklist: ChecklistDb?
    @State private var isTriggersChecklistPresented = false
    
    private let shortcutPublisher: AnyPublisher<ShortcutDb, Never> = Utils_kmpKt.uiShortcutFlow.toPublisher()
    private let checklistPublisher: AnyPublisher<ChecklistDb, Never> = Utils_kmpKt.uiChecklistFlow.toPublisher()
    
    var body: some View {
        
        VMView(vm: vm, stack: .ZStack(alignment: .bottom)) { state in
            
            /// # PROVOKE_STATE_UPDATE
            EmptyView().id("MainView checklist \(triggersChecklist?.id ?? 0)")
            
            VStack {
                
                HomeTimerView(vm: vm, state: state)
                
                ZStack {
                    
                    let checklistDb = state.checklistDb
                    
                    VStack {
                        
                        if let readmeMessage = state.readmeMessage {
                            Button(
                                action: {
                                    vm.onReadmeOpen()
                                    navigation.path.append(.readme(defaultItem: .basics))
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
                                itemHeight: Float(HomeView__MTG_ITEM_HEIGHT)
                            )
                            
                            VStack {
                                
                                if let checklistDb = checklistDb {
                                    VStack {
                                        ChecklistView(
                                            checklistDb: checklistDb,
                                            onDelete: {},
                                            maxLines: 1,
                                            bottomPadding: 0
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
                                                .frame(width: .infinity)
                                                .clipShape(roundedShape)
                                                
                                                HStack {
                                                    
                                                    Text(goalBarUi.textLeft)
                                                        .padding(.leading, mtgCircleHPadding)
                                                        .foregroundColor(c.white)
                                                        .font(.system(size: mtgCircleFontSize, weight: mtgCircleFontWeight))
                                                    
                                                    Spacer()
                                                    
                                                    Text(goalBarUi.textRight)
                                                        .padding(.trailing, mtgCircleHPadding)
                                                        .foregroundColor(c.white)
                                                        .font(.system(size: mtgCircleFontSize, weight: mtgCircleFontWeight))
                                                }
                                            }
                                            .frame(height: mtgCircleHeight, alignment: .center)
                                            .background(roundedShape.fill(c.homeFg))
                                            .padding(.horizontal, H_PADDING)
                                        }
                                        .frame(height: HomeView__MTG_ITEM_HEIGHT, alignment: .center)
                                        .offset(y: 1)
                                    }
                                )
                            }
                        )
                        
                        Padding(vertical: 10.0)
                    }
                    .padding(.bottom, HomeView__BOTTOM_NAVIGATION_HEIGHT)
                    
                    if (state.isTasksVisible) {
                        TasksView()
                            .clipped() // Fix list offset on IME open
                            .padding(.bottom, HomeView__BOTTOM_NAVIGATION_HEIGHT)
                    }
                }
            }
            
            HomeTabsView(vm: vm, state: state)
        }
        .ignoresSafeArea(.keyboard, edges: .bottom)
        .onReceive(shortcutPublisher) { shortcut in
            let swiftURL = URL(string: shortcut.uri)!
            if !UIApplication.shared.canOpenURL(swiftURL) {
                Utils_kmpKt.showUiAlert(message: "Invalid shortcut link", reportApiText: nil)
                return
            }
            UIApplication.shared.open(swiftURL)
        }
        .onReceive(checklistPublisher) { checklist in
            triggersChecklist = checklist
            isTriggersChecklistPresented = true
        }
        .sheetEnv(isPresented: $isTriggersChecklistPresented) {
            if let checklist = triggersChecklist {
                ChecklistSheet(isPresented: $isTriggersChecklistPresented, checklist: checklist)
            }
        }
    }
}

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
                            .font(.system(size: mtgCircleFontSize, weight: mtgCircleFontWeight))
                            .padding(.horizontal, mtgCircleHPadding)
                            .frame(height: mtgCircleHeight)
                            .background(roundedShape.fill(timeUI.textBgColor.toColor()))
                            .padding(.trailing, mainTask.taskUi.tf.paused != nil ? 9 : 8)
                    }

                    if mainTask.taskUi.tf.paused != nil {
                        ZStack {
                            Image(systemName: "pause")
                                .foregroundColor(c.white)
                                .font(.system(size: 12, weight: .black))
                        }
                        .frame(width: mtgCircleHeight, height: mtgCircleHeight)
                        .background(roundedShape.fill(c.green))
                        .padding(.trailing, 8)
                    }

                    Text(mainTask.text)
                        .font(.system(size: HomeView__PRIMARY_FONT_SIZE))
                        .foregroundColor(Color.white)
                        .padding(.trailing, 4)

                    Spacer()

                    if let timeUI = mainTask.timeUI {
                        Text(timeUI.note)
                            .foregroundColor(timeUI.noteColor.toColor())
                            .font(.system(size: HomeView__PRIMARY_FONT_SIZE))
                    }
                }
                .frame(height: HomeView__MTG_ITEM_HEIGHT)
                .padding(.horizontal, H_PADDING)
            }
        )
    }
}
