import SwiftUI
import shared

let HomeTabsView__HEIGHT = 56.0

private let menuTimeFont = buildTimerFont(size: 10)

struct HomeTabsView: View {
    
    let vm: HomeVm
    let state: HomeVm.State
    @Binding var tabSelected: HomeTabSelected?

    ///
    
    @EnvironmentObject private var nativeSheet: NativeSheet
    
    var body: some View {
        
        HStack(alignment: .bottom) {

            Button(
                action: {
                    nativeSheet.showActivitiesTimerSheet(
                        timerContext: nil,
                        withMenu: true,
                        onStart: {}
                    )
                },
                label: {
                    VStack {
                        Spacer()
                        Image(systemName: "timer")
                            .frame(height: HomeTabsView__HEIGHT)
                            .foregroundColor(c.homeFontSecondary)
                            .font(.system(size: 30, weight: .thin))
                            .frame(maxWidth: .infinity)
                            .frame(alignment: .bottom)
                    }
                }
            )

            Button(
                action: {
                    vm.toggleIsTasksVisible()
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
                    .background(state.isTasksVisible ? Color(.systemGray5) : .black)
                    .cornerRadius(10, onTop: true, onBottom: true)
                }
            )

            Button(
                action: {
                    tabSelected = (tabSelected == nil ? .settings : nil)
                },
                label: {
                    VStack {
                        Spacer()
                        Image(systemName: "ellipsis.circle")
                            .frame(height: HomeTabsView__HEIGHT)
                            .foregroundColor(c.homeFontSecondary)
                            .font(.system(size: 30, weight: .thin))
                            .frame(maxWidth: .infinity)
                    }
                }
            )
        }
        .fillMaxWidth()
        .frame(height: HomeTabsView__HEIGHT)
    }
}
