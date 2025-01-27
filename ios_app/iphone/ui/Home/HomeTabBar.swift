import SwiftUI
import shared

let HomeTabBar__HEIGHT = 56.0

private let menuTimeFont = buildTimerFont(size: 10)

extension View {
    
    func contentMarginsTabBar(extra: CGFloat = 0) -> some View {
        contentMargins(.bottom, HomeTabBar__HEIGHT + extra)
    }
}

struct HomeTabBar: View {
    
    let vm: HomeVm
    let state: HomeVm.State
    
    @Binding var tabSelected: HomeTabSelected
    
    ///
    
    private var showBackground: Bool {
        tabSelected != .main || state.isTasksVisible
    }
    
    private var background: AnyShapeStyle {
        // Not .clear to tap area for onTouchGesture()
        showBackground ? AnyShapeStyle(.bar) : AnyShapeStyle(.black)
    }
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    
    var body: some View {
        
        ZStack(alignment: .top) {
            
            HStack {
                
                Image(systemName: "timer")
                    .fillMaxSize()
                    .foregroundColor(c.homeFontSecondary)
                    .font(.system(size: 30, weight: .thin))
                    .background(background)
                    .onTouchGesture {
                        nativeSheet.showActivitiesTimerSheet(
                            timerContext: nil,
                            withMenu: true,
                            onStart: {}
                        )
                    }
                
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
                .padding(.top, 3)
                .frame(height: HomeTabBar__HEIGHT)
                .frame(maxWidth: .infinity)
                .background(background)
                .onTouchGesture {
                    if tabSelected == .main {
                        vm.toggleIsTasksVisible()
                    } else {
                        vm.setIsTaskVisible(isVisible: false)
                        tabSelected = .main
                    }
                }
                
                Image(systemName: "ellipsis.circle")
                    .fillMaxSize()
                    .foregroundColor(tabSelected == .settings ? .blue : c.homeFontSecondary)
                    .font(.system(size: 30, weight: .thin))
                    .background(background)
                    .onTouchGesture {
                        vm.setIsTaskVisible(isVisible: false)
                        tabSelected = (tabSelected == .settings ? .main : .settings)
                    }
            }
            .fillMaxWidth()
            .frame(height: HomeTabBar__HEIGHT)
            
            if showBackground {
                DividerBg()
            }
        }
    }
}
