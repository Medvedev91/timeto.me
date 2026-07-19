import SwiftUI
import shared

struct HomeTaskView: View {
    
    let homeTaskUi: HomeTasksItemUi.HomeTaskUi
    let homeModeTaskFolder: HomeMode.TaskFolder
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    @State private var xSwipeOffset: CGFloat = 0
    @State private var width: CGFloat? = nil
    
    var body: some View {
        
        let taskDb: TaskDb = homeTaskUi.taskUi.taskDb
        
        ZStack {
            
            GeometryReader { proxy in
                ZStack {
                }
                .onChange(of: proxy.size.width, initial: true) { _, newValue in
                    width = newValue
                }
                .onChange(of: proxy.frame(in: .global).minY) {
                    xSwipeOffset = 0
                }
            }
            .frame(height: 1) // height: 1, or full height items if short
            
            if (xSwipeOffset < 0) {
                HomeTaskStaEndView(
                    onMoveToTimer: {
                        Haptic.mediumShot()
                        homeTaskUi.taskUi.moveToTimer()
                    },
                    onDelete: {
                        Haptic.mediumShot()
                        homeTaskUi.taskUi.delete()
                    },
                    onCancel: {
                        withAnimation {
                            xSwipeOffset = 0
                        }
                    },
                )
            } else if (xSwipeOffset > 0) {
                HomeTaskStaStartView(
                    homeTaskUi: homeTaskUi,
                    onCancel: {
                        withAnimation {
                            xSwipeOffset = 0
                        }
                    }
                )
            }
            
            ///

            Button(
                action: {
                    taskDb.startIntervalForUi(
                        ifJustStarted: {},
                        ifTimerNeeded: {
                            navigation.showTaskTimerSheet(
                                taskDb: taskDb,
                            )
                        },
                    )
                },
                label: {
                    
                    HStack {
                        
                        if let timeUi = homeTaskUi.timeUi {
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
                                .padding(.trailing, homeTaskUi.taskUi.tf.paused != nil ? 9 : HomeScreen__itemCircleMarginTrailing)
                        }
                        
                        if homeTaskUi.taskUi.tf.paused != nil {
                            ZStack {
                                Image(systemName: "pause")
                                    .foregroundColor(.white)
                                    .font(.system(size: 12, weight: .black))
                            }
                            .frame(width: HomeScreen__itemCircleHeight, height: HomeScreen__itemCircleHeight)
                            .background(roundedShape.fill(.green))
                            .padding(.trailing, 8)
                        }
                        
                        if let activityUi = homeTaskUi.taskUi.activityUi {
                            let symbol: Symbol = activityUi.symbol
                            HStack {
                                
                                let offsetX: CGFloat = {
                                    if symbol is Symbol.Letter {
                                        return 0
                                    }
                                    if symbol is Symbol.Icon {
                                        return -2
                                    }
                                    if symbol is Symbol.Emoji {
                                        return -2
                                    }
                                    return 0
                                }()
                                
                                SymbolView(
                                    symbol: symbol,
                                    color: activityUi.colorRgba.toColor(),
                                    letterSize: HomeScreen__primaryFontSize,
                                    iconSize: 15,
                                    emojiSize: HomeScreen__itemCircleFontSize,
                                )
                                .offset(x: offsetX)
                                
                                Spacer()
                            }
                            .frame(width: 20)
                        }
                        
                        Text(homeTaskUi.text)
                            .font(.system(size: HomeScreen__primaryFontSize))
                            .foregroundColor(.white)
                            .padding(.trailing, 4)
                        
                        Spacer()
                        
                        if let timeUi = homeTaskUi.timeUi {
                            let noteColor: Color = switch timeUi.status {
                            case .in: .secondary
                            case .soon: .blue
                            case .overdue: .red
                            default: fatalError("timeUi.status noteColor not handled")
                            }
                            Text(timeUi.note)
                                .foregroundColor(noteColor)
                                .font(.system(size: HomeScreen__primaryFontSize))
                                .padding(.trailing, HomeScreen__itemCircleHPadding)
                        }
                        
                        if (homeModeTaskFolder.taskFolderDb.activity_id != nil) && (taskDb.isToday || taskDb.isTomorrow) {
                            HomeBarTaskFolderButton(
                                taskFolderUi: homeTaskUi.taskUi.taskFolderUi,
                                color: taskDb.isToday ? .orange : .indigo,
                                onClick: {
                                    homeTaskUi.taskUi.updateTaskFolder(
                                        taskFolderDb: homeModeTaskFolder.taskFolderDb,
                                    )
                                },
                            )
                        }
                    }
                    .frame(height: HomeScreen__itemHeight)
                    .padding(.leading, HomeScreen__hPadding)
                }
            )
            .background(.black)
            .offset(x: xSwipeOffset)
            .highPriorityGesture(gesture)
        }
    }
    
    // https://stackoverflow.com/a/79037514
    private var gesture: some Gesture {
        DragGesture(minimumDistance: 25, coordinateSpace: .global)
            .onChanged { value in
                xSwipeOffset = value.translation.width
            }
            .onEnded { value in
                if value.translation.width < -80 {
                    xSwipeOffset = (width ?? 999) * -1
                } else if value.translation.width > 60 {
                    xSwipeOffset = width ?? 999
                } else {
                    xSwipeOffset = 0
                }
            }
    }
}
