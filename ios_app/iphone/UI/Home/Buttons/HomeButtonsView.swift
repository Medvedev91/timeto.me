import SwiftUI
import shared

private let rowHeight: CGFloat = HomeScreen__itemHeight
private let spacing: CGFloat = 8
private let buttonsHPadding: CGFloat = HomeScreen__hPadding

struct HomeButtonsView: View {
    
    var body: some View {
        VmView({
            HomeButtonsVm(
                width: Float(calcWidth()),
                rowHeight: Float(rowHeight),
                spacing: Float(spacing)
            )
        }) { _, state in
            HomeButtonsViewInner(
                state: state
            )
            .fillMaxWidth()
            .frame(height: CGFloat(state.height))
        }
        .padding(.horizontal, buttonsHPadding)
    }
}

private struct HomeButtonsViewInner: View {
    
    let state: HomeButtonsVm.State
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.clear
            
            ForEach(state.buttonsUi, id: \.id) { buttonUi in
                ZStack {
                    if let goal = buttonUi.type as? HomeButtonType.Goal {
                        HomeButtonGoalView(goal: goal)
                    } else {
                        fatalError()
                    }
                }
                .frame(width: CGFloat(buttonUi.width), height: rowHeight)
                .offset(x: CGFloat(buttonUi.offsetX), y: CGFloat(buttonUi.offsetY))
            }
        }
        .fillMaxSize()
    }
}

private func calcWidth() -> CGFloat {
    UIScreen.main.bounds.size.width - (buttonsHPadding * 2)
}
