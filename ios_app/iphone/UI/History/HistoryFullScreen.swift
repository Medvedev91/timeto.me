import SwiftUI
import shared

struct HistoryFullScreen: View {
    
    let onClose: () -> Void
    
    var body: some View {
        VmView({
            HistoryVm()
        }) { vm, state in
            HistoryFullScreenInner(
                vm: vm,
                state: state,
                onClose: onClose
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

//
// Warning! #InitScrollBugFix
// It is extremely tricky to make scroll to bottom working well.
// The same bug https://developer.apple.com/forums/thread/741406
// I had to use many tricks:
// - #InitScrollBugFix_AfterLoadView
// - #InitScrollBugFix_SectionSpacing
// - #InitScrollBugFix_OnChange
// - #InitScrollBugFix_FirstDaySize
// - #InitScrollBugFix_VStack
// Please check it out before working with code.
// Hopefully it will be fixed in the next versions of SwiftUI.

private let barPxSecondsRatio: Int = 50

private struct HistoryFullScreenInner: View {
    
    let vm: HistoryVm
    let state: HistoryVm.State
    
    let onClose: () -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation
    
    @State private var initScrollBugFixVar: Bool = false
    private let initScrollBugFixAfterLoadViewId = "initScrollBugFixViewId"
    
    var body: some View {
        
        ScrollViewReader { scrollProxy in
            
            ScrollView(.vertical, showsIndicators: false) {
                
                // #InitScrollBugFix_SectionSpacing
                // Warning! WTF?! Without "spacing: 8+" the trick does not work!
                // It has something to do with Section->header, so without header it works!
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
                            
                            // #InitScrollBugFix_VStack
                            // Warning! WTF?! Without VStack the trick does not work!
                            VStack(spacing: 10) {
                                
                                ForEach(dayUi.intervalsUi, id: \.intervalDb.id) { intervalUi in
                                    
                                    HStack(alignment: .top, spacing: 10) {
                                        
                                        Text(intervalUi.timeString)
                                            .monospaced()
                                            .fontWeight(.semibold)
                                            .foregroundColor(!intervalUi.isStartsPrevDay ? .primary : .clear)
                                        
                                        VStack {
                                            RoundedRectangle(cornerRadius: 20, style: .continuous)
                                                .fill(intervalUi.activityDb.colorRgba.toColor())
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
                                    .padding(.leading, 20)
                                    .padding(.trailing, 20)
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
                            // #InitScrollBugFix_FirstDaySize
                            // As I understand, SwiftUI predicts next elements height by the first one.
                            // So we have to make first element full day height.
                            .padding(.top, CGFloat(dayUi.secondsFromDayStartIosFix.toInt() / barPxSecondsRatio))
                        }
                    }
                    
                    //
                    // #InitScrollBugFix_AfterLoadView
                    //
                    // The main trick!
                    // After loading data adding the view and scroll to that view.
                    // Please look at #InitScrollBugFix_OnChange
                    //
                    // Note, because of LazyVStack(spacing: 16) we already have bottom
                    // content padding Between the last section and this element, in fact
                    // replacement for .contentMargins(.bottom, 16).
                    if initScrollBugFixVar {
                        ZStack {}
                            .frame(height: onePx)
                            .id(initScrollBugFixAfterLoadViewId)
                    }
                }
            }
            .defaultScrollAnchor(.bottom)
            // #InitScrollBugFix_OnChange
            .onChange(of: state.daysUi) {
                if !initScrollBugFixVar {
                    initScrollBugFixVar = true
                    for i in 0...10 {
                        let delay: CGFloat = 0.001 + (CGFloat(i) * 0.05)
                        myAsyncAfter(delay) {
                            scrollProxy.scrollTo(initScrollBugFixAfterLoadViewId, anchor: .bottom)
                        }
                    }
                }
            }
        }
        .toolbar {
            ToolbarItemGroup(placement: .bottomBar) {
                BottomBarAddButton(
                    text: "New Entry",
                    action: {
                        navigation.sheet {
                            HistoryFormSheet(
                                initIntervalDb: nil
                            )
                        }
                    }
                )
                Spacer()
                Button("Close") {
                    dismiss()
                    onClose()
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
