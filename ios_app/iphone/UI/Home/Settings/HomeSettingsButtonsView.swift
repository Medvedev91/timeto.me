import SwiftUI
import shared

private let rowHeight: CGFloat = HomeScreen__itemHeight
private let barHeight: CGFloat = HomeScreen__itemCircleHeight
private let spacing: CGFloat = 8.0
// todo from Vm?
private let cellsCount: Int = 6
// todo remove
private let matrixSize: Int = 5

struct HomeSettingsButtonsView: View {
    
    var body: some View {
        GeometryReader { geometry in
            // todo
            let width: CGFloat = geometry.size.width
            let cellWidth: CGFloat = (width - (spacing * CGFloat(cellsCount - 1))) / CGFloat(cellsCount)
            ButtonsView(
                width: width,
                cellWidth: cellWidth,
                matrix: buildMatrix(cellWidth: cellWidth)
            )
        }
        .padding(.horizontal, H_PADDING)
        .frame(height: CGFloat(matrixSize) * rowHeight)
    }
}

private struct ButtonsView: View {
    
    let width: CGFloat
    var cellWidth: CGFloat
    let matrix: [[MatrixItem]]
    
    ///
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.clear
            ForEachIndexed(matrix) { _, matrixRow in
                ForEachIndexed(matrixRow) { _, item in
                    DragItemView(
                        matrixItem: item,
                        onDrag: { x, y in
                            let items: [MatrixItem] = matrix.reduce([], +)
                            let nearest: MatrixItem = items.min { a, b in
                                let aRange = abs(a.initX - x) + abs(a.initY - y)
                                let bRange = abs(b.initX - x) + abs(b.initY - y)
                                return aRange < bRange
                            }!
                            zlog("nearest \(nearest.idxRow) \(nearest.start)")
                        }
                    )
                    .id("row-\(item.idxRow)-item-\(item.start)")
                    .frame(
                        width: abs((cellWidth * CGFloat(item.cells)) + (CGFloat(item.cells - 1) * spacing)),
                        height: rowHeight
                    )
                }
            }
        }
        .fillMaxSize()
    }
}

private struct DragItemView: View {
    
    let matrixItem: MatrixItem
    let onDrag: (_ x: CGFloat, _ y: CGFloat) -> Void
    
    ///
    
    @GestureState private var locationState = CGPoint(x: 0, y: 0)
    @State private var s2 = CGPoint(x: 0, y: 0)
    @State private var isDrag: Bool = false
    
    private var offset: CGPoint {
        CGPoint(x: s2.x + matrixItem.initX, y: s2.y + matrixItem.initY)
    }
    
    var body: some View {
        
        ZStack {
            ZStack {}
                .fillMaxWidth()
                .frame(height: barHeight)
                .background(roundedShape.fill(matrixItem.color))
        }
        .fillMaxSize()
        //
        .offset(x: offset.x, y: offset.y)
        .zIndex(isDrag ? 2 : 1)
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
                    onDrag(offset.x, offset.y)
//                    dragItem = DragItem(x: offset.x, y: offset.y)
//                    print("go \(offset.x) \(offset.y)")
                }
                .onEnded { _ in
                    print(";; end")
                    isDrag = false
//                    dragItem = nil
                }
        )
    }
}

private struct DragItem {
    let x: CGFloat
    let y: CGFloat
}

private let barBgColor = Color(.systemGray5)

private func buildMatrix(
    cellWidth: CGFloat
) -> [[MatrixItem]] {
    return [
        buildEmptyRow(idxRow: 0, cellWidth: cellWidth),
        [
            MatrixItem(start: 0, cells: 2, color: .red, idxRow: 1, cellWidth: cellWidth),
            MatrixItem(start: 2, cells: 3, color: .blue, idxRow: 1, cellWidth: cellWidth),
            MatrixItem(start: 5, cells: 1, color: barBgColor, idxRow: 1, cellWidth: cellWidth),
        ],
        buildEmptyRow(idxRow: 2, cellWidth: cellWidth),
        [
            MatrixItem(start: 0, cells: 2, color: .purple, idxRow: 3, cellWidth: cellWidth),
            MatrixItem(start: 2, cells: 1, color: barBgColor, idxRow: 3, cellWidth: cellWidth),
            MatrixItem(start: 3, cells: 3, color: .cyan, idxRow: 3, cellWidth: cellWidth),
        ],
        buildEmptyRow(idxRow: 4, cellWidth: cellWidth),
    ]
}

private func buildEmptyRow(
    idxRow: Int,
    cellWidth: CGFloat
) -> [MatrixItem] {
    [
        MatrixItem(start: 0, cells: 1, color: barBgColor, idxRow: idxRow, cellWidth: cellWidth),
        MatrixItem(start: 1, cells: 1, color: barBgColor, idxRow: idxRow, cellWidth: cellWidth),
        MatrixItem(start: 2, cells: 1, color: barBgColor, idxRow: idxRow, cellWidth: cellWidth),
        MatrixItem(start: 3, cells: 1, color: barBgColor, idxRow: idxRow, cellWidth: cellWidth),
        MatrixItem(start: 4, cells: 1, color: barBgColor, idxRow: idxRow, cellWidth: cellWidth),
        MatrixItem(start: 5, cells: 1, color: barBgColor, idxRow: idxRow, cellWidth: cellWidth),
    ]
}

private struct MatrixItem {
    
    let start: Int
    let cells: Int
    let color: Color
    
    let idxRow: Int
    
    let initX: CGFloat
    let initY: CGFloat
    
    init(
        start: Int,
        cells: Int,
        color: Color,
        idxRow: Int,
        cellWidth: CGFloat
    ) {
        self.start = start
        self.cells = cells
        self.color = color
        self.idxRow = idxRow
        self.initX = (CGFloat(start) * cellWidth) + (CGFloat(start) * spacing)
        self.initY = CGFloat(idxRow) * rowHeight
    }
}

///

private struct DataGridItem: Identifiable {
    let id: UUID = UUID()
    let rowIdx: Int
    let cellStartIdx: Int
    let cellsSize: Int
    let color: Color
}

private struct DbDataItem {
    let rowIdx: Int
    let cellStartIdx: Int
    let cellsSize: Int
    let color: Color
}

private struct ViewGridItem {
    
    let data: DataGridItem
    
    let initX: CGFloat
    let initY: CGFloat
    
    init(
        data: DataGridItem,
        cellWidth: CGFloat
    ) {
        self.data = data
        self.initX = (CGFloat(data.cellStartIdx) * cellWidth) + (CGFloat(data.cellStartIdx) * spacing)
        self.initY = CGFloat(data.rowIdx) * rowHeight
    }
}

private func buildViewGrid(
    cellWidth: CGFloat
) -> [ViewGridItem] {
    buildDataGrid().map { item in
        ViewGridItem(data: item, cellWidth: cellWidth)
    }
}

private func buildEmptyDataGridRow(
    rowIdx: Int
) -> [DataGridItem] {
    (0..<cellsCount).map { cellIdx in
        DataGridItem(rowIdx: rowIdx, cellStartIdx: cellIdx, cellsSize: 1, color: Color(.systemGray5))
    }
}

private func buildDataGrid() -> [DataGridItem] {
    var list: [DataGridItem] = [
        DataGridItem(rowIdx: 1, cellStartIdx: 0, cellsSize: 2, color: .red),
        DataGridItem(rowIdx: 1, cellStartIdx: 2, cellsSize: 3, color: .blue),
        DataGridItem(rowIdx: 3, cellStartIdx: 0, cellsSize: 2, color: .purple),
        DataGridItem(rowIdx: 3, cellStartIdx: 3, cellsSize: 3, color: .cyan),
    ]
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 0))
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 1))
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 2))
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 3))
    list.append(contentsOf: buildEmptyDataGridRow(rowIdx: 4))
    return list
}

/*
 private func buildDataGrid() -> [DataGridItem] {
 let dbData: [DbDataItem] = [
 DbDataItem(rowIdx: 0, cellStartIdx: 0, cellsSize: 2, color: .red),
 DbDataItem(rowIdx: 0, cellStartIdx: 2, cellsSize: 3, color: .blue),
 DbDataItem(rowIdx: 1, cellStartIdx: 0, cellsSize: 2, color: .purple),
 DbDataItem(rowIdx: 1, cellStartIdx: 3, cellsSize: 3, color: .cyan),
 ]
 // todo !!
 let maxDbRowIdx: Int = dbData.map { $0.rowIdx }.max()!
 var dataGrid: [DataGridItem] = []
 dataGrid.append(contentsOf: buildEmptyDataGridRow(rowIdx: 0))
 dbData.enumerated().forEach { idx, dbItem in
 dataGrid.append(
 DataGridItem(
 rowIdx: dbItem.rowIdx * 2 + 1,
 cellStartIdx: dbItem.cellStartIdx,
 cellsSize: dbItem.cellsSize
 )
 )
 //        dataGrid.append(contentsOf: buildEmptyDataGridRow(rowIdx: idx))
 }
 return dataGrid
 }
*/
