import SwiftUI
import shared

private let rowHeight: CGFloat = HomeScreen__itemHeight
private let barHeight: CGFloat = HomeScreen__itemCircleHeight
private let spacing: CGFloat = 8.0
// todo from Vm?
private let cellsCount: Int = 6

struct HomeSettingsButtonsView: View {
    
    var body: some View {
        GeometryReader { geometry in
            ButtonsView(
                width: geometry.size.width
            )
        }
        .padding(.horizontal, H_PADDING)
        .frame(height: CGFloat(matrix.count) * rowHeight)
    }
}

private struct ButtonsView: View {
    
    let width: CGFloat
    
    ///
    
    private var cellWidth: CGFloat {
        (width - (spacing * CGFloat(cellsCount - 1))) / CGFloat(cellsCount)
    }
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.clear
            ForEachIndexed(matrix) { idxRow, matrixRow in
                ForEachIndexed(matrixRow) { idxItem, item in
                    DragItem(matrixItem: item)
                        .id("row-\(idxRow)-item-\(idxItem)")
                        .frame(
                            width: abs((cellWidth * CGFloat(item.cells)) + (CGFloat(item.cells - 1) * spacing)),
                            height: rowHeight
                        )
                        .offset(
                            x: (CGFloat(item.start) * cellWidth) + (CGFloat(item.start) * spacing),
                            y: CGFloat(idxRow) * rowHeight
                        )
                }
            }
        }
        .fillMaxSize()
    }
}

private struct DragItem: View {
    
    let matrixItem: MatrixItem
    
    ///
    
    @GestureState private var locationState = CGPoint(x: 0, y: 0)
    @State private var s2 = CGPoint(x: 0, y: 0)
    @State private var isDrag: Bool = false
    
    var body: some View {
        
        ZStack {
            ZStack {}
                .fillMaxWidth()
                .frame(height: barHeight)
                .background(roundedShape.fill(matrixItem.color))
        }
        .fillMaxSize()
        //
        .offset(x: s2.x, y: s2.y)
        .zIndex(isDrag ? 2 : 1)
        .gesture(
            DragGesture(
                // minimumDistance: 10,
                // coordinateSpace: .global
            )
            .updating($locationState) { currentState, gestureState, transaction in
                // todo remove?
                isDrag = true
                gestureState = currentState.location
                s2 = CGPoint(x: gestureState.x - currentState.startLocation.x, y: gestureState.y - currentState.startLocation.y)
                print("go \(gestureState.x) \(currentState.startLocation.x)")
            }
                .onEnded { _ in
                    print(";; end")
                    isDrag = false
                }
        )
    }
}

///

private let barBgColor = Color(.systemGray5)

private let emptyMatrixRow: [MatrixItem] = [
    MatrixItem(start: 0, cells: 1, color: barBgColor),
    MatrixItem(start: 1, cells: 1, color: barBgColor),
    MatrixItem(start: 2, cells: 1, color: barBgColor),
    MatrixItem(start: 3, cells: 1, color: barBgColor),
    MatrixItem(start: 4, cells: 1, color: barBgColor),
    MatrixItem(start: 5, cells: 1, color: barBgColor),
]

private let matrix: [[MatrixItem]] = [
    [
        MatrixItem(start: 0, cells: 2, color: .red),
        MatrixItem(start: 2, cells: 3, color: .blue),
        MatrixItem(start: 5, cells: 1, color: barBgColor),
    ],
    emptyMatrixRow,
    [
        MatrixItem(start: 0, cells: 2, color: .purple),
        MatrixItem(start: 2, cells: 1, color: barBgColor),
        MatrixItem(start: 3, cells: 3, color: .cyan),
    ],
    emptyMatrixRow,
]

private struct MatrixItem {
    let start: Int
    let cells: Int
    let color: Color
}
