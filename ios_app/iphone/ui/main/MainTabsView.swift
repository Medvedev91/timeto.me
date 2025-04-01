import SwiftUI
import shared

let MainTabsView__HEIGHT = 56.0

extension View {
    
    func contentMarginsTabBar(extra: CGFloat = 0) -> some View {
        contentMargins(.bottom, MainTabsView__HEIGHT + extra)
    }
}

struct MainTabsView: View {
    
    @Binding var tab: MainTabEnum
    
    var body: some View {
        VmView({
            MainTabsVm()
        }) { _, state in
            MainTabsViewInner(
                state: state,
                tab: $tab
            )
        }
    }
}

///

private let menuTimeFont: Font = buildTimerFont(size: 10)

private struct MainTabsViewInner: View {
    
    let state: MainTabsVm.State
    
    @Binding var tab: MainTabEnum
    
    ///
    
    private var showBackground: Bool {
        tab != .home
    }
    
    var body: some View {
        
        ZStack(alignment: .top) {
            
            HStack {
                
                Button(
                    action: {
                        tab = (tab == .activities ? .home : .activities)
                    },
                    label: {
                        Image(systemName: "timer")
                            .fillMaxSize()
                            .foregroundColor(tab == .activities ? .blue : c.homeFontSecondary)
                            .font(.system(size: 30, weight: .thin))
                    }
                )
                
                Button(
                    action: {
                        tab = (tab == .home ? .tasks : .home)
                    },
                    label: {
                        
                        VStack(alignment: .center) {
                            
                            Text(state.timeText)
                                .foregroundColor(c.homeMenuTime)
                                .font(menuTimeFont)
                                .padding(.top, 3)
                                .padding(.bottom, 7)
                            
                            HStack {
                                
                                let batteryUi = state.batteryUi
                                let batteryTextColor = batteryUi.colorRgba.toColor()
                                
                                Image(systemName: "bolt.fill")
                                    .foregroundColor(batteryTextColor)
                                    .font(.system(size: 12, weight: batteryUi.isHighlighted ? .regular : .ultraLight))
                                
                                Text(batteryUi.text)
                                    .foregroundColor(batteryTextColor)
                                    .font(.system(size: 13, weight: batteryUi.isHighlighted ? .bold : .regular))
                                
                                Image(systemName: "smallcircle.filled.circle")
                                    .foregroundColor(c.homeFontSecondary)
                                    .font(.system(size: 11 + halfDpCeil, weight: .regular))
                                    .padding(.leading, 6)
                                    .padding(.trailing, 1 + halfDpFloor)
                                
                                Text(state.tasksText)
                                    .foregroundColor(c.homeFontSecondary)
                                    .font(.system(size: 13, weight: .regular))
                            }
                            .padding(.trailing, 2)
                        }
                        .padding(.top, 3)
                        .frame(height: MainTabsView__HEIGHT)
                        .frame(maxWidth: .infinity)
                    }
                )
                
                Button(
                    action: {
                        tab = (tab == .settings ? .home : .settings)
                    },
                    label: {
                        Image(systemName: "ellipsis.circle")
                            .fillMaxSize()
                            .foregroundColor(tab == .settings ? .blue : c.homeFontSecondary)
                            .font(.system(size: 30, weight: .thin))
                    }
                )
            }
            .fillMaxWidth()
            .frame(height: MainTabsView__HEIGHT)
            
            if showBackground {
                DividerBg()
            }
        }
        .background(showBackground ? AnyShapeStyle(.bar) : AnyShapeStyle(.clear))
        .onChange(of: state.lastIntervalId) { _, new in
            tab = .home
            Haptic.mediumShot()
        }
    }
}
