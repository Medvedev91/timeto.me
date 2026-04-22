import SwiftUI
import shared

struct HomeTasksView: View {
    
    let homeVm: HomeVm
    let homeState: HomeVm.State
    
    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack {
                ForEach(homeState.mainListItemsUi.reversed(), id: \.id) { mainListItemUi in
                    if let taskItemUi = mainListItemUi as? HomeVm.MainListItemUiMainTaskUi {
                        TaskItemView(
                            mainListItemUi: taskItemUi,
                            showOnHomeActivity:
                                !homeState.onHomeActivity && (taskItemUi.taskUi.taskDb.folder_id == homeState.activityTaskFolderDb?.id),
                        )
                    } else if let barItemUi = mainListItemUi as? HomeVm.MainListItemUiTaskFolderBarUi {
                        TaskFolderBarView(
                            barUi: barItemUi,
                            onHomeActivity: homeState.onHomeActivity,
                            toggleOnHomeActivity: {
                                homeVm.toggleOnHomeActivity()
                            },
                        )
                    }
                }
            }
        }
        .defaultScrollAnchor(.bottom)
    }
}

///

private struct TaskItemView: View {
    
    let mainListItemUi: HomeVm.MainListItemUiMainTaskUi
    let showOnHomeActivity: Bool
    
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        Button(
            action: {
                mainListItemUi.taskUi.taskDb.startIntervalForUi(
                    ifJustStarted: {},
                    ifTimerNeeded: {
                        navigation.showTaskTimerSheet(
                            taskDb: mainListItemUi.taskUi.taskDb
                        )
                    }
                )
            },
            label: {
                
                HStack {
                    
                    if let timeUi = mainListItemUi.timeUi {
                        let bgColor: Color = switch timeUi.status {
                        case .in: homeFgColor
                        case .soon: .blue
                        case .overdue: .red
                        default: fatalError("timeUi.status bgColor not handled")
                        }
                        Text(timeUi.text)
                            .foregroundColor(.white)
                            .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                            .padding(.horizontal, HomeScreen__itemCircleHPadding)
                            .frame(height: HomeScreen__itemCircleHeight)
                            .background(roundedShape.fill(bgColor))
                            .padding(.trailing, mainListItemUi.taskUi.tf.paused != nil ? 9 : HomeScreen__itemCircleMarginTrailing)
                    }
                    
                    if mainListItemUi.taskUi.tf.paused != nil {
                        ZStack {
                            Image(systemName: "pause")
                                .foregroundColor(.white)
                                .font(.system(size: 12, weight: .black))
                        }
                        .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
                        .background(roundedShape.fill(.green))
                        .padding(.trailing, 8)
                    }
                    
                    Text(mainListItemUi.text)
                        .font(.system(size: HomeScreen__primaryFontSize))
                        .foregroundColor(.white)
                        .padding(.trailing, 4)
                    
                    Spacer()
                    
                    if let timeUi = mainListItemUi.timeUi {
                        let noteColor: Color = switch timeUi.status {
                        case .in: .secondary
                        case .soon: .blue
                        case .overdue: .red
                        default: fatalError("timeUi.status noteColor not handled")
                        }
                        Text(timeUi.note)
                            .foregroundColor(noteColor)
                            .font(.system(size: HomeScreen__primaryFontSize))
                    }
                    
                    if showOnHomeActivity {
                        Button(
                            action: {
                                mainListItemUi.toggleOnHomeActivity()
                            },
                            label: {
                                Image(systemName: "house")
                                    .foregroundColor(mainListItemUi.taskUi.taskDb.onHomeActivity ? .secondary : homeFgColor)
                                    .font(.system(size: 19, weight: .semibold))
                            },
                        )
                        .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
                        .buttonStyle(.plain)
                        .padding(.top, onePx)
                    }
                }
                .frame(height: HomeScreen__itemHeight)
                .padding(.horizontal, HomeScreen__hPadding)
            }
        )
    }
}

private struct TaskFolderBarView: View {
    
    let barUi: HomeVm.MainListItemUiTaskFolderBarUi
    let onHomeActivity: Bool
    let toggleOnHomeActivity: () -> Void
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    var body: some View {
        HStack {
            
            Button(
                action: {
                    navigation.showTaskForm(
                        strategy: TaskFormStrategy.NewTask(
                            taskFolderDb: barUi.taskFolderDb,
                        )
                    )
                },
                label: {
                    HStack {
                        
                        ZStack {
                            Image(systemName: "plus")
                                .foregroundColor(.black)
                                .font(.system(size: 14, weight: .bold))
                        }
                        .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
                        .background(roundedShape.fill(.blue))
                        
                        Text("New Task")
                            .foregroundColor(.white)
                            .font(.system(size: HomeScreen__primaryFontSize))
                            .padding(.leading, 8)
                    }
                },
            )
            
            Spacer()
            
            Button(
                action: {
                    toggleOnHomeActivity()
                },
                label: {
                    Image(systemName: "house")
                        .foregroundColor(onHomeActivity ? .secondary : homeFgColor)
                        .font(.system(size: 19, weight: .semibold))
                },
            )
            .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
            .buttonStyle(.plain)
            .padding(.top, onePx)
            
            if barUi.todayTasksCount > 0 {
                Button(
                    action: {
                        barUi.toggleCollapseToday()
                    },
                    label: {
                        ZStack {
                            if (barUi.isCollapsed) {
                                Text("\(barUi.todayTasksCount)")
                                    .foregroundColor(.secondary)
                                    .font(.system(size: HomeScreen__itemCircleFontSize, weight: HomeScreen__itemCircleFontWeight))
                            } else {
                                Image(systemName: "chevron.down")
                                    .foregroundColor(.secondary)
                                    .font(.system(size: 12, weight: .bold))
                            }
                        }
                        .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
                        .background(
                            Circle()
                                .strokeBorder(.secondary, lineWidth: 2)
                        )
                    },
                )
                .buttonStyle(.plain)
                .padding(.leading, 10)
            }
        }
        .frame(height: HomeScreen__itemHeight)
        .padding(.horizontal, HomeScreen__hPadding)
    }
}
