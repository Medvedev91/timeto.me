import SwiftUI
import shared

struct HistoryScreen: View {
    
    @Binding var tab: MainTabEnum
    
    var body: some View {
        VmView({
            HistoryVm()
        }) { vm, state in
            HistoryScreenInner(
                vm: vm,
                state: state,
                tab: $tab,
            )
        }
        .onAppear {
            MainTabsView.autoTabChange = false
        }
        .onDisappear {
            MainTabsView.autoTabChange = true
        }
    }
}

private let barPxSecondsRatio: Int = 50

//
// #ScrollBug https://developer.apple.com/forums/thread/741406
//
private struct HistoryScreenInner: View {
    
    let vm: HistoryVm
    let state: HistoryVm.State
    
    @Binding var tab: MainTabEnum
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    @State private var scrollPosition = ScrollPosition()
    
    private let timer60 = Timer.publish(every: 60, on: .current, in: .common).autoconnect()
    
    // Fix case if LazyVStack shorter than ScrollView,
    // relevant for just installed history.
    @State private var scrollViewHeight: CGFloat = 0
    @State private var shortScrollViewBugFixPadding: CGFloat = 0
    @State private var scrollDisabled = false
    
    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            
            Spacer()
                .frame(height: shortScrollViewBugFixPadding)
            
            LazyVStack(spacing: 16, pinnedViews: [.sectionHeaders]) {
                
                ForEach(state.daysUi, id: \.unixDay) { dayUi in
                    
                    ZStack {                        }
                        .frame(height: 1)
                        .onAppear {
                            if state.daysUi.first == dayUi, tab == .activity, !scrollDisabled {
                                scrollDisabled = true
                                scrollPosition.scrollTo(id: dayUi.unixDay, anchor: .top)
                                vm.loadNext {
                                    scrollPosition.scrollTo(id: dayUi.unixDay, anchor: .top)
                                    for i in 1...10 {
                                        let delay = Double(i) * 0.02
                                        myAsyncAfter(delay) {
                                            scrollPosition.scrollTo(id: dayUi.unixDay, anchor: .top)
                                        }
                                    }
                                    myAsyncAfter(0.2) {
                                        scrollDisabled = false
                                    }
                                }
                            }
                        }
                    
                    Section(
                        header: Button(
                            action: {
                                // todo dialog to move to day
                                withAnimation {
                                    // scrollPosition.scrollTo(id: "day_20185", anchor: .top)
                                }
                            },
                            label: {
                                Text(dayUi.dayText)
                                    .padding(.horizontal, 9)
                                    .padding(.vertical, 6)
                                    .background(Capsule().fill(.blue))
                                    .foregroundColor(.white)
                                    .font(.system(size: 14))
                            }
                        )
                        .id("day_\(dayUi.unixDay)")
                    ) {
                        
                        ForEach(dayUi.intervalsUi, id: \.listId) { intervalUi in
                            
                            HStack(alignment: .top, spacing: 10) {
                                
                                Text(intervalUi.timeString)
                                    .monospaced()
                                    .fontWeight(.semibold)
                                    .foregroundColor(!intervalUi.isStartsPrevDay ? .primary : .clear)
                                
                                VStack {
                                    RoundedRectangle(cornerRadius: 20, style: .continuous)
                                        .fill(intervalUi.goalDb.colorRgba.toColor())
                                        .frame(
                                            width: 10,
                                            height: Double(10.limitMin(intervalUi.secondsForBar.toInt() / barPxSecondsRatio))
                                        )
                                        .padding(.leading, 5)
                                        .padding(.trailing, 5)
                                }
                                .frame(maxHeight: .infinity)
                                
                                HStack(alignment: .top, spacing: 8) {
                                    
                                    if !intervalUi.isStartsPrevDay {
                                        
                                        Text(intervalUi.text)
                                            .fontWeight(.medium)
                                            .textAlign(.leading)
                                            .foregroundColor(.primary)
                                        
                                        Spacer()
                                        
                                        Text(intervalUi.periodString)
                                            .fontWeight(.light)
                                            .foregroundColor(.secondary)
                                    }
                                }
                                .fillMaxWidth()
                            }
                            .padding(.leading, 10)
                            .padding(.trailing, 10)
                            .background(.background) // Tap area for context menu
                            .contextMenu {
                                Section {
                                    Button(
                                        action: {
                                            showIntervalForm(intervalDb: intervalUi.intervalDb)
                                        },
                                        label: {
                                            Label("Edit", systemImage: "square.and.pencil")
                                        }
                                    )
                                }
                                Section {
                                    Button(
                                        action: {
                                            vm.moveIntervalToTasks(
                                                intervalDb: intervalUi.intervalDb,
                                                dialogsManager: navigation
                                            )
                                        },
                                        label: {
                                            Label(HistoryFormUtils.shared.moveToTasksTitle, systemImage: "arrow.uturn.backward")
                                                .foregroundColor(.orange)
                                        }
                                    )
                                    Button(
                                        role: .destructive,
                                        action: {
                                            vm.deleteInterval(
                                                intervalDb: intervalUi.intervalDb,
                                                dialogsManager: navigation
                                            )
                                        },
                                        label: {
                                            Label("Delete", systemImage: "trash")
                                                .foregroundColor(.red)
                                        }
                                    )
                                }
                            }
                            .onTapGesture {
                                showIntervalForm(intervalDb: intervalUi.intervalDb)
                            }
                        }
                    }
                }
                
                //
                // Fight With #ScrollBug
                // Side effect contentMargins(..)-like effect
                Spacer()
                    .frame(height: 44)
            }
            .onGeometryChange(for: CGSize.self) { $0.size } action: {
                //
                // Fight for Performance
                // DO NOT USE shortScrollViewBugFixPadding as calculating variable.
                //
                let lazyVStackHeight = $0.height
                let diff = scrollViewHeight - lazyVStackHeight
                if diff > 0.01 {
                    shortScrollViewBugFixPadding = diff
                } else if shortScrollViewBugFixPadding > 0.01 {
                    shortScrollViewBugFixPadding = 0
                }
            }
            // Fight With #ScrollBug
            .id("LazyVStack: \(state.daysUi.hashValue)")
        }
        // .top to right autoload from top
        .scrollPosition($scrollPosition, anchor: .top)
        .defaultScrollAnchor(.bottom)
        .scrollDisabled(scrollDisabled)
        .onChange(of: tab) { oldTab, newTab in
            if newTab == .activity {
                scrollPosition.scrollTo(edge: .bottom)
                myAsyncAfter(0.1) {
                    vm.restartDaysUiIfLess1Min {
                        scrollPosition.scrollTo(edge: .bottom)
                    }
                }
            }
            if oldTab == .activity {
                vm.restartDaysUi {}
            }
        }
        .onReceive(timer60) { _ in
            if tab != .activity {
                vm.restartDaysUi {}
            }
        }
        .onGeometryChange(for: CGSize.self) { $0.size } action: { scrollViewHeight = $0.height }
    }
    
    private func showIntervalForm(
        intervalDb: IntervalDb
    ) {
        navigation.sheet {
            HistoryFormSheet(
                initIntervalDb: intervalDb
            )
        }
    }
}
