import SwiftUI
import shared

private let rowHeight: CGFloat = 26
private let barHeight: CGFloat = 24
private let spacing: CGFloat = 10

private var resizeDotViewArcRadius: CGFloat = barHeight / 2
private var resizeDotViewArcLineWidth: CGFloat = 4

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
                state: state
            )
            .frame(height: CGFloat(state.buttonsData.rowsCount) * rowHeight)
        }
        .padding(.horizontal, buttonsHPadding)
    }
}

private struct ButtonsView: View {
    
    let vm: HomeSettingsVm
    let state: HomeSettingsVm.State
    
    ///
    
    @State private var hoverButtonsUi: [HomeSettingsButtonUi] = []
    @State private var ignoreNextHaptic: Bool = true
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            Color.clear
            
            ForEach(state.buttonsData.emptyButtonsUi, id: \.id) { buttonUi in
                ButtonView(
                    buttonUi: buttonUi,
                    extraLeftWidth: .constant(0),
                    extraRightWidth: .constant(0),
                    content: {}
                )
            }
            
            ForEach(state.buttonsData.dataButtonsUi, id: \.id) { buttonUi in
                DragButtonView(
                    buttonUi: buttonUi,
                    onDrag: { cgPoint in
                        hoverButtonsUi = vm.getHoverButtonsUiOnDrag(
                            buttonUi: buttonUi,
                            x: Float(cgPoint.x),
                            y: Float(cgPoint.y)
                        )
                    },
                    onDragEnd: { cgPoint in
                        hoverButtonsUi = []
                        Task {
                            // To run onChange() for hoverButtonsUi before this
                            try? await Task.sleep(nanoseconds: 1_000)
                            ignoreNextHaptic = true
                        }
                        return vm.onButtonDragEnd(
                            buttonUi: buttonUi,
                            x: Float(cgPoint.x),
                            y: Float(cgPoint.y)
                        )
                    },
                    onResize: { left, right in
                        hoverButtonsUi = vm.getHoverButtonsUiOnResize(
                            buttonUi: buttonUi,
                            left: Float(left),
                            right: Float(right)
                        )
                    },
                    onResizeEnd: { left, right in
                        hoverButtonsUi = []
                        Task {
                            // To run onChange() for hoverButtonsUi before this
                            try? await Task.sleep(nanoseconds: 1_000)
                            ignoreNextHaptic = true
                        }
                        return vm.onButtonResizeEnd(
                            buttonUi: buttonUi,
                            left: Float(left),
                            right: Float(right)
                        )
                    }
                )
            }
            
            ForEach(hoverButtonsUi, id: \.id) { buttonUi in
                ButtonView(
                    buttonUi: buttonUi,
                    extraLeftWidth: .constant(0),
                    extraRightWidth: .constant(0),
                    content: {}
                )
            }
        }
        .fillMaxSize()
        .onChange(of: hoverButtonsUi) { _, new in
            if new.isEmpty {
                return
            }
            if ignoreNextHaptic {
                ignoreNextHaptic = false
                return
            }
            Haptic.softShot()
        }
    }
}

private struct ButtonView<Content>: View where Content: View {
    
    let buttonUi: HomeSettingsButtonUi
    @Binding var extraLeftWidth: CGFloat
    @Binding var extraRightWidth: CGFloat
    @ViewBuilder var content: () -> Content
    
    private var offset: CGPoint {
        CGPoint(x: CGFloat(buttonUi.initX), y: CGFloat(buttonUi.initY))
    }
    
    var body: some View {
        ZStack {
            content()
        }
        .fillMaxWidth()
        .frame(height: barHeight)
        .background(roundedShape.fill(buttonUi.colorRgba.toColor()))
        .offset(x: offset.x - extraLeftWidth, y: offset.y)
        .frame(
            width: CGFloat(buttonUi.fullWidth) + extraLeftWidth + extraRightWidth,
            height: rowHeight
        )
    }
}

private struct DragButtonView: View {
    
    let buttonUi: HomeSettingsButtonUi
    
    let onDrag: (CGPoint) -> Void
    let onDragEnd: (CGPoint) -> Bool
    
    let onResize: (_ left: CGFloat, _ right: CGFloat) -> Void
    let onResizeEnd: (_ left: CGFloat, _ right: CGFloat) -> Bool
    
    ///
    
    @State private var onTop: Bool = false
    
    @State private var dragLocalOffset = CGPoint(x: 0, y: 0)
    private var dragGlobalOffset: CGPoint {
        CGPoint(
            x: dragLocalOffset.x + CGFloat(buttonUi.initX),
            y: dragLocalOffset.y + CGFloat(buttonUi.initY)
        )
    }
    
    @State private var resizeOffsetLeft: CGFloat = 0
    @State private var resizeOffsetRight: CGFloat = 0

    var body: some View {
        ZStack {
            ButtonView(
                buttonUi: buttonUi,
                extraLeftWidth: $resizeOffsetLeft,
                extraRightWidth: $resizeOffsetRight,
                content: {
                    HStack {
                        
                        ResizeDotView(
                            onResize: { value in
                                onTop = true
                                resizeOffsetLeft = max(
                                    min(value * -1, CGFloat(buttonUi.resizeLeftMaxOffset)),
                                    CGFloat(buttonUi.resizeLeftMinOffset)
                                )
                                onResize(resizeOffsetLeft, resizeOffsetRight)
                            },
                            onResizeEnd: { _ in
                                let isPositionChanged = onResizeEnd(resizeOffsetLeft, resizeOffsetRight)
                                if !isPositionChanged {
                                    withAnimation {
                                        resizeOffsetLeft = 0
                                    }
                                }
                                onTop = false
                            }
                        )
                        .rotationEffect(.degrees(180.0))
                        .offset(x: -2)
                        
                        Spacer()
                        
                        ResizeDotView(
                            onResize: { value in
                                onTop = true
                                resizeOffsetRight = min(value, CGFloat(buttonUi.resizeRightMaxOffset))
                                onResize(resizeOffsetLeft, resizeOffsetRight)
                            },
                            onResizeEnd: { _ in
                                let isPositionChanged = onResizeEnd(resizeOffsetLeft, resizeOffsetRight)
                                if !isPositionChanged {
                                    withAnimation {
                                        resizeOffsetRight = 0
                                    }
                                }
                                onTop = false
                            }
                        )
                        .offset(x: 2)
                    }
                }
            )
        }
        .offset(x: dragLocalOffset.x, y: dragLocalOffset.y)
        .zIndex(onTop ? 2 : 1)
        .gesture(
            DragGesture()
                .onChanged { value in
                    onTop = true
                    dragLocalOffset = CGPoint(
                        x: value.translation.width,
                        y: value.translation.height
                    )
                    onDrag(dragGlobalOffset)
                }
                .onEnded { _ in
                    let isPositionChanged = onDragEnd(dragGlobalOffset)
                    if !isPositionChanged {
                        withAnimation {
                            dragLocalOffset = CGPoint(x: 0, y: 0)
                        }
                    }
                    onTop = false
                }
        )
    }
}

private struct ResizeDotView: View {
    
    let onResize: (CGFloat) -> Void
    let onResizeEnd: (CGFloat) -> Void
    
    ///
    
    private let size: CGFloat = barHeight - 16
    
    var body: some View {
        
        ResizeDotViewArc(startAngle: .degrees(70), endAngle: .degrees(290), clockwise: true)
            .stroke(.white.opacity(0.9), style: .init(lineWidth: resizeDotViewArcLineWidth, lineCap: .round))
            .frame(width: resizeDotViewArcRadius, height: barHeight)
            .gesture(
                DragGesture(coordinateSpace: .global)
                    .onChanged { value in
                        onResize(value.translation.width)
                    }
                    .onEnded { value in
                        onResizeEnd(value.translation.width)
                    }
            )
    }
}

private struct ResizeDotViewArc: Shape {
    
    let startAngle: Angle
    let endAngle: Angle
    let clockwise: Bool

    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.addArc(
            center: CGPoint(x: 0, y: resizeDotViewArcRadius),
            radius: resizeDotViewArcRadius - (resizeDotViewArcLineWidth / 2),
            startAngle: startAngle,
            endAngle: endAngle,
            clockwise: clockwise
        )
        return path
    }
}

private func calcCellWidth() -> CGFloat {
    let cellsCount: Int = HomeSettingsVm.companion.cellsCount.toInt()
    let width: CGFloat = UIScreen.main.bounds.size.width - (buttonsHPadding * 2)
    return (width - (spacing * CGFloat(cellsCount - 1))) / CGFloat(cellsCount)
}
