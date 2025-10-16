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

private struct HistoryScreenInner: View {
    
    let vm: HistoryVm
    let state: HistoryVm.State
    
    @Binding var tab: MainTabEnum
    
    ///
    
    @Environment(Navigation.self) private var navigation
    
    // Tricky bugfix from isZhous https://developer.apple.com/forums/thread/741406
    @State private var scrollPosition = ScrollPosition()
    
    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            
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
            }
        }
        .scrollPosition($scrollPosition, anchor: .bottom)
        .defaultScrollAnchor(.bottom)
        .contentMargins(.bottom, 16)
        .onChange(of: tab) { _, newTab in
            if newTab == .activities {
                scrollPosition.scrollTo(edge: .bottom)
                myAsyncAfter(0.1) {
                    vm.updateDaysUi {
                        scrollPosition.scrollTo(edge: .bottom)
                    }
                }
            }
        }
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
