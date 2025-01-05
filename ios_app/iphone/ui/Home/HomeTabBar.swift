import SwiftUI
import shared

let HomeTabsView__HEIGHT = 56.0

private let menuTimeFont = buildTimerFont(size: 10)

struct HomeTabBar: View {
    
    let vm: HomeVm
    let state: HomeVm.State
    @Binding var tabSelected: HomeTabSelected
    
    ///
    
    private var showBackground: Bool {
        tabSelected != .main || state.isTasksVisible
    }
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    
    var body: some View {
        
        ZStack(alignment: .top) {
            
            HStack {
                
                Button(
                    action: {
                        nativeSheet.showActivitiesTimerSheet(
                            timerContext: nil,
                            withMenu: true,
                            onStart: {}
                        )
                    },
                    label: {
                        Image(systemName: "timer")
                            .fillMaxSize()
                            .foregroundColor(c.homeFontSecondary)
                            .font(.system(size: 30, weight: .thin))
                    }
                )
                
                Button(
                    action: {
                        if tabSelected == .main {
                            vm.toggleIsTasksVisible()
                        } else {
                            vm.setIsTaskVisible(isVisible: false)
                            tabSelected = .main
                        }
                    },
                    label: {
                        
                        VStack(alignment: .center) {
                            
                            Text(state.menuTime)
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
                                
                                Text(state.menuTasksNote)
                                    .foregroundColor(c.homeFontSecondary)
                                    .font(.system(size: 13, weight: .regular))
                            }
                            .padding(.trailing, 2)
                        }
                        .padding(.top, 2)
                        .frame(height: HomeTabsView__HEIGHT)
                        .frame(maxWidth: .infinity)
                    }
                )
                
                Button(
                    action: {
                        vm.setIsTaskVisible(isVisible: false)
                        tabSelected = (tabSelected == .settings ? .main : .settings)
                    },
                    label: {
                        Image(systemName: "ellipsis.circle")
                            .fillMaxSize()
                            .foregroundColor(tabSelected == .settings ? .blue : c.homeFontSecondary)
                            .font(.system(size: 30, weight: .thin))
                    }
                )
            }
            .fillMaxWidth()
            .frame(height: HomeTabsView__HEIGHT)
            
            if showBackground {
                DividerBg()
            }
        }
        .background(showBackground ? AnyShapeStyle(.bar) : AnyShapeStyle(.clear))
    }
}
