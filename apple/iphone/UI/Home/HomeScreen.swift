import SwiftUI
import shared

let HomeScreen__primaryFontSize: CGFloat = 18

let HomeScreen__hPadding: CGFloat = 8
let HomeScreen__itemHeight: CGFloat = 38
let HomeScreen__itemCircleHPadding: CGFloat = 7
let HomeScreen__itemCircleHeight: CGFloat = 24
let HomeScreen__itemCircleFontSize: CGFloat = 15
let HomeScreen__itemCircleFontWeight: Font.Weight = .semibold
let HomeScreen__itemCircleMarginTrailing: CGFloat = 8
let HomeScreen__secondaryColor: Color = MainTabsVm.companion.menuSecondaryColorDark.toColor()

struct HomeScreen: View {
    
    var body: some View {
        VmView({
            HomeVm()
        }) { vm, state in
            let state = vm.state.value as! HomeVm.State
            HomeScreenInner(
                vm: vm,
                state: state
            )
        }
    }
}

///

private struct HomeScreenInner: View {
    
    let vm: HomeVm
    let state: HomeVm.State
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        
        let isTodayTaskFolder: Bool = (state.homeMode as? HomeMode.TaskFolder)?.taskFolderDb.isToday == true
        
        VStack {
            
            let checklistDb: ChecklistDb? = state.checklistDb
            
            if isTodayTaskFolder {
                
                HomeTimerView(vm: vm, state: state)
                
                if let whatsNewMessage = state.whatsNewMessage {
                    MessageButton(
                        title: whatsNewMessage,
                        onTap: {
                            navigation.push(.whatsNew)
                        },
                    )
                }
                
                if let privacyMessage = state.privacyMessage {
                    MessageButton(
                        title: privacyMessage,
                        onTap: {
                            navigation.fullScreen {
                                PrivacyScreen(
                                    toForceChoice: true,
                                    titleDisplayMode: .large,
                                    scrollBottomMargin: MainTabsView__HEIGHT,
                                )
                            }
                        },
                    )
                }
                
                if let checklistHintUi = state.checklistHintUi {
                    HomeChecklistHintView(hintUi: checklistHintUi)
                }
            }
            
            GeometryReader { geometry in
                
                let _ = vm.upListsContainerSize(
                    totalHeight: Float(geometry.size.height),
                    itemHeight: Float(HomeScreen__itemHeight)
                )
                
                VStack {
                    
                    if let homeMode = state.homeMode as? HomeMode.TaskFolder {
                        
                        let isToday: Bool = homeMode.taskFolderDb.isToday
                        let isMainListItemsExists: Bool = !homeMode.homeTasksItemsUi.isEmpty
                        
                        if let checklistDb = checklistDb, isToday {
                            VStack {
                                ChecklistView(
                                    checklistDb: checklistDb,
                                    maxLines: 1,
                                    withAddButton: false,
                                    onDelete: {},
                                )
                            }
                            .frame(height: CGFloat(state.listsSizes.checklist))
                        }
                        
                        if isMainListItemsExists || !isToday {
                            HomeTasksView(
                                homeModeTaskFolder: homeMode,
                            )
                            .frame(height: !isToday ? .infinity : CGFloat(state.listsSizes.mainTasks))
                        }
                        
                        Spacer()
                    } else if let homeMode = state.homeMode as? HomeMode.NoteFolder {
                        HomeNotesView(
                            noteFolderDb: homeMode.noteFolderDb,
                        )
                    } else {
                        // todo sealed
                    }
                }
            }
            
            if let notificationsPermissionUi = state.notificationsPermissionUi {
                HomeNotificationsView(notificationsPermissionUi: notificationsPermissionUi)
            }
            
            if state.showDocBanner {
                HomeReadmeView(
                    title: state.readmeTitle,
                    buttonText: state.readmeButtonText,
                )
            }
            
            if state.showRate {
                HomeRateView(
                    homeVm: vm,
                    homeState: state,
                )
            }
            
            HomeBarView(
                homeBarUi: state.homeBarUi,
                changeTaskFolder: { taskFolderUi in
                    vm.updateTaskFolder(taskFolderUi: taskFolderUi)
                },
                changeNoteFolder: { noteFolderUi in
                    vm.updateNoteFolder(noteFolderUi: noteFolderUi)
                },
            )
            
            HomeButtonsView()
            
            Padding(vertical: 10.0)
        }
        .padding(.bottom, MainTabsView__HEIGHT)
        .onChange(of: state.forceOpenDoc, initial: true) { old, newValue in
            if newValue {
                navigation.fullScreen {
                    DocFullScreen(
                        forceRead: true
                    )
                }
            }
        }
    }
}

private struct MessageButton: View {
    
    let title: String
    let onTap: () -> Void
    
    var body: some View {
        Button(
            action: {
                onTap()
            },
            label: {
                Text(title)
                    .foregroundColor(.white)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                    .font(.system(size: 17, weight: .medium))
                    .background(roundedShape.fill(.red))
                    .padding(.vertical, 8)
            }
        )
    }
}
