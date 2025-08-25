import SwiftUI
import shared

let MainTabsView__HEIGHT = 56.0

private let menuTimeFont: Font = buildTimerFont(size: 10)

private let menuPrimaryColor: Color = MainTabsVm.companion.menuPrimaryColorDark.toColor()
private let menuSecondaryColor: Color = HomeScreen__secondaryColor

extension View {
    
    func contentMarginsTabBar(extra: CGFloat = 0) -> some View {
        contentMargins(.bottom, MainTabsView__HEIGHT + extra)
    }
}

struct MainTabsView: View {
    
    @Binding var tab: MainTabEnum
    
    static var autoTabChange: Bool = true
    
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
                            .foregroundColor(tab == .activities ? .blue : menuSecondaryColor)
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
                                .foregroundColor(menuPrimaryColor)
                                .font(menuTimeFont)
                                .padding(.top, 3)
                                .padding(.bottom, 7)
                            
                            HStack {
                                
                                let batteryUi = state.batteryUi
                                let batteryTextColor: Color = batteryUi.colorEnum?.toColor() ?? menuSecondaryColor
                                
                                Image(systemName: "bolt.fill")
                                    .foregroundColor(batteryTextColor)
                                    .font(.system(size: 12, weight: batteryUi.isHighlighted ? .regular : .ultraLight))
                                
                                Text(batteryUi.text)
                                    .foregroundColor(batteryTextColor)
                                    .font(.system(size: 13, weight: batteryUi.isHighlighted ? .bold : .regular))
                                
                                Text(state.dateText)
                                    .foregroundColor(menuSecondaryColor)
                                    .font(.system(size: 13, weight: .regular))
                                    .padding(.leading, 4)
                            }
                            .padding(.trailing, 1)
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
                            .foregroundColor(tab == .settings ? .blue : menuSecondaryColor)
                            .font(.system(size: 30, weight: .thin))
                    }
                )
            }
            .fillMaxWidth()
            .frame(height: MainTabsView__HEIGHT)
            
            if showBackground {
                Divider()
                    .opacity(0.3)
            }
        }
        .background(showBackground ? AnyShapeStyle(.bar) : AnyShapeStyle(.clear))
        .onChange(of: state.lastIntervalId) { _, new in
            if MainTabsView.autoTabChange {
                tab = .home
            }
            Haptic.mediumShot()
        }
    }
}
