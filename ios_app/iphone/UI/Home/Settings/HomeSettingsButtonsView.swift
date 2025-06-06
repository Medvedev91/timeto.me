import SwiftUI
import shared

private let rowHeight: CGFloat = HomeScreen__itemHeight
private let barHeight: CGFloat = HomeScreen__itemCircleHeight
private let spacing: CGFloat = 8.0

// todo remove?
private let zIndexBg: Double = 1
private let zIndexHover: Double = 2
private let zIndexBar: Double = 3
private let zIndexDrag: Double = 4

private let barBgColor = Color(.systemGray5)
private let cellHoverColor = Color(.systemGray2)

private let buttonsHPadding: CGFloat = H_PADDING

struct HomeSettingsButtonsView: View {
    
    private let cellWidth: CGFloat = calcCellWidth()
    
    var body: some View {
        VmView({
            HomeSettingsVm(
                spacing: Float(spacing),
                cellWidth: Float(cellWidth),
                rowHeight: Float(rowHeight)
            )
        }) { vm, state in
            ButtonsView(
                vm: vm,
                state: state,
                cellWidth: cellWidth
            )
            .frame(height: CGFloat(state.rowsCount) * rowHeight)
        }
        .padding(.horizontal, H_PADDING)
    }
}

private struct ButtonsView: View {
    
    let vm: HomeSettingsVm
    let state: HomeSettingsVm.State
    
    // todo remove?
    let cellWidth: CGFloat
    
    @State private var hoverGridItems: [HomeSettingsButtonUi] = []
    
    ///
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.clear
            
            ForEach(state.emptyButtonsUi, id: \.id) { buttonUi in
                GridItemView(
                    viewGridItem: buttonUi,
                    cellWidth: cellWidth
                )
            }
            
            ForEach(state.dataButtonsUi, id: \.id) { buttonUi in
                DragGridItemView(
                    viewGridItem: buttonUi,
                    cellWidth: cellWidth,
                    onDrag: { x, y in
                        hoverGridItems = vm.calcHoverButtonsUi(x: Float(x), y: Float(y))
                    }
                )
            }
            
            ForEach(hoverGridItems, id: \.id) { item in
                GridItemView(
                    viewGridItem: item,
                    cellWidth: cellWidth
                )
            }
        }
        .fillMaxSize()
    }
}

private struct GridItemView: View {
    
    let viewGridItem: HomeSettingsButtonUi
    // todo remove
    let cellWidth: CGFloat
    
    private var offset: CGPoint {
        CGPoint(x: CGFloat(viewGridItem.initX), y: CGFloat(viewGridItem.initY))
    }
    
    var body: some View {
        ZStack {}
            .fillMaxWidth()
            .frame(height: barHeight)
            .background(roundedShape.fill(viewGridItem.colorRgba.toColor()))
            .offset(x: offset.x, y: offset.y)
            .frame(
                width: abs(
                    (cellWidth * CGFloat(viewGridItem.cellsSize)) +
                    (CGFloat(viewGridItem.cellsSize - 1) * spacing)
                ),
                height: rowHeight
            )
    }
}

private struct DragGridItemView: View {
    
    let viewGridItem: HomeSettingsButtonUi
    let cellWidth: CGFloat
    let onDrag: (_ x: CGFloat, _ y: CGFloat) -> Void
    
    ///
    
    @GestureState private var locationState = CGPoint(x: 0, y: 0)
    @State private var s2 = CGPoint(x: 0, y: 0)
    @State private var isDrag: Bool = false
    
    private var offset: CGPoint {
        CGPoint(x: s2.x, y: s2.y)
    }
    
    private var zIndex: Double {
        isDrag ? 2 : 1
    }
    
    var body: some View {
        GridItemView(
            viewGridItem: viewGridItem,
            cellWidth: cellWidth
        )
        .offset(x: offset.x, y: offset.y)
        .zIndex(zIndex)
        .gesture(
            DragGesture()
                .updating($locationState) { currentState, gestureState, transaction in
                    isDrag = true
                    // todo remove?
                    gestureState = currentState.location
                    s2 = CGPoint(
                        x: gestureState.x - currentState.startLocation.x,
                        y: gestureState.y - currentState.startLocation.y
                    )
                    onDrag(offset.x + CGFloat(viewGridItem.initX), offset.y + CGFloat(viewGridItem.initY))
                }
                .onEnded { _ in
                    print(";; end")
                    isDrag = false
                }
        )
    }
}

private func calcCellWidth() -> CGFloat {
    let cellsCount: Int = HomeSettingsVm.companion.cellsCount.toInt()
    let width: CGFloat = UIScreen.main.bounds.size.width - (buttonsHPadding * 2)
    return (width - (spacing * CGFloat(cellsCount - 1))) / CGFloat(cellsCount)
}
