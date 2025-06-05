import SwiftUI
import shared

private let rowHeight: CGFloat = HomeScreen__itemHeight
private let barHeight: CGFloat = HomeScreen__itemCircleHeight
private let spacing: CGFloat = 8.0
// todo from Vm?
private let cellsCount: Int = 6
// todo remove
private let matrixSize: Int = 5

// todo remove?
private let zIndexBg: Double = 1
private let zIndexHover: Double = 2
private let zIndexBar: Double = 3
private let zIndexDrag: Double = 4

private let barBgColor = Color(.systemGray5)
private let cellHoverColor = Color(.systemGray2)

struct HomeSettingsButtonsView: View {
    
    var body: some View {
        GeometryReader { geometry in
            // todo
            let width: CGFloat = geometry.size.width
            let cellWidth: CGFloat = (width - (spacing * CGFloat(cellsCount - 1))) / CGFloat(cellsCount)
            ButtonsView(
                width: width,
                cellWidth: cellWidth,
                viewGridItems: buildViewGrid(cellWidth: cellWidth),
                bgViewGridItems: buildBgViewGrid(cellWidth: cellWidth)
            )
        }
        .padding(.horizontal, H_PADDING)
        .frame(height: CGFloat(matrixSize) * rowHeight)
    }
}

private struct ButtonsView: View {
    
    let width: CGFloat
    let cellWidth: CGFloat
    let viewGridItems: [ViewGridItem]
    let bgViewGridItems: [ViewGridItem]
    
    @State private var hoverGridItems: [ViewGridItem] = []
    
    ///
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.clear
            
            ForEach(bgViewGridItems, id: \.id) { item in
                GridItemView(
                    viewGridItem: item,
                    cellWidth: cellWidth
                )
            }

            ForEach(viewGridItems, id: \.id) { item in
                DragGridItemView(
                    viewGridItem: item,
                    cellWidth: cellWidth,
                    onDrag: { x, y in
                        let nearest: ViewGridItem = bgViewGridItems.min { a, b in
                            let aRange = abs(a.initX - x) + abs(a.initY - y)
                            let bRange = abs(b.initX - x) + abs(b.initY - y)
                            return aRange < bRange
                        }!
                        
                        var newGridItem = nearest
                        newGridItem.color = cellHoverColor
                        hoverGridItems = [newGridItem]
                        zlog("nearest \(nearest.rowIdx) \(nearest.cellStartIdx)")
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
    
    let viewGridItem: ViewGridItem
    let cellWidth: CGFloat
    
    private var offset: CGPoint {
        CGPoint(x: viewGridItem.initX, y: viewGridItem.initY)
    }
    
    var body: some View {
        ZStack {}
            .fillMaxWidth()
            .frame(height: barHeight)
            .background(roundedShape.fill(viewGridItem.color))
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
    
    let viewGridItem: ViewGridItem
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
                    onDrag(offset.x + viewGridItem.initX, offset.y + viewGridItem.initY)
                }
                .onEnded { _ in
                    print(";; end")
                    isDrag = false
                }
        )
    }
}

private struct ViewGridItem {
    
    let id: UUID = UUID()
    let rowIdx: Int
    let cellStartIdx: Int
    let cellsSize: Int
    var color: Color
    
    let initX: CGFloat
    let initY: CGFloat
    
    init(
        rowIdx: Int,
        cellStartIdx: Int,
        cellsSize: Int,
        color: Color,
        cellWidth: CGFloat
    ) {
        self.rowIdx = rowIdx
        self.cellStartIdx = cellStartIdx
        self.cellsSize = cellsSize
        self.color = color
        self.initX = (CGFloat(cellStartIdx) * cellWidth) + (CGFloat(cellStartIdx) * spacing)
        self.initY = CGFloat(rowIdx) * rowHeight
    }
}

private func buildViewGrid(
    cellWidth: CGFloat
) -> [ViewGridItem] {
    [
        ViewGridItem(rowIdx: 1, cellStartIdx: 0, cellsSize: 2, color: .red, cellWidth: cellWidth),
        ViewGridItem(rowIdx: 1, cellStartIdx: 2, cellsSize: 3, color: .blue, cellWidth: cellWidth),
        ViewGridItem(rowIdx: 3, cellStartIdx: 0, cellsSize: 2, color: .purple, cellWidth: cellWidth),
        ViewGridItem(rowIdx: 3, cellStartIdx: 3, cellsSize: 3, color: .cyan, cellWidth: cellWidth),
    ]
}

private func buildEmptyDataGridRow(
    rowIdx: Int,
    cellWidth: CGFloat
) -> [ViewGridItem] {
    (0..<cellsCount).map { cellIdx in
        ViewGridItem(rowIdx: rowIdx, cellStartIdx: cellIdx, cellsSize: 1, color: barBgColor, cellWidth: cellWidth)
    }
}

private func buildBgViewGrid(
    cellWidth: CGFloat
) -> [ViewGridItem] {
    var list: [ViewGridItem] = []
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 0, cellWidth: cellWidth))
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 1, cellWidth: cellWidth))
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 2, cellWidth: cellWidth))
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 3, cellWidth: cellWidth))
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 4, cellWidth: cellWidth))
    return list

}
