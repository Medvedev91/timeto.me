import SwiftUI
import shared

let HomeScreen__ITEM_HEIGHT: CGFloat = 38
let HomeScreen__PRIMARY_FONT_SIZE: CGFloat = 18

let HomeScreen__itemCircleHPadding: CGFloat = 7
let HomeScreen__itemCircleHeight: CGFloat = 24
let HomeScreen__itemCircleFontSize: CGFloat = 15
let HomeScreen__itemCircleFontWeight: Font.Weight = .semibold

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
                        HomeTasksView(
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
                                            .padding(.leading, HomeScreen__itemCircleHPadding)
                                            .foregroundColor(c.white)
                                            .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                                        
                                        Spacer()
                                        
                                        Text(goalBarUi.textRight)
                                            .padding(.trailing, HomeScreen__itemCircleHPadding)
                                            .foregroundColor(c.white)
                                            .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                                    }
                                }
                                .frame(height: HomeScreen__itemCircleHeight, alignment: .center)
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
