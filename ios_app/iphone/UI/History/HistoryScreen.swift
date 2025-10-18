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
    @State private var lazyVStackHeight: CGFloat = 0
    private var shortScrollViewBugFixPadding: CGFloat {
        max(0, scrollViewHeight - lazyVStackHeight)
    }
    
    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            
            Spacer()
                .frame(height: shortScrollViewBugFixPadding)
            
            LazyVStack(spacing: 16, pinnedViews: [.sectionHeaders]) {
                
                ForEach(state.daysUi, id: \.unixDay) { dayUi in
                    
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
                // Fix for #ScrollBug
                // Side effect contentMargins(..)-like effect
                Spacer()
                    .frame(height: 1)
            }
            .onGeometryChange(for: CGSize.self) { $0.size } action: { lazyVStackHeight = $0.height }
        }
        .scrollPosition($scrollPosition, anchor: .bottom)
        .defaultScrollAnchor(.bottom)
        .onChange(of: tab) { oldTab, newTab in
            if newTab == .activity {
                scrollPosition.scrollTo(edge: .bottom)
                myAsyncAfter(0.1) {
                    vm.restartDaysUiIfLess1Min()
                }
            }
            if oldTab == .activity {
                vm.restartDaysUi()
            }
        }
        .onReceive(timer60) { _ in
            if tab != .activity {
                vm.restartDaysUi()
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
