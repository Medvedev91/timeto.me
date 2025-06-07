import SwiftUI
import shared

private let rowHeight: CGFloat = 26
private let barHeight: CGFloat = 24
private let spacing: CGFloat = 10

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
                    extraRightWidth: .constant(0),
                    extraLeftWidth: .constant(0),
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
                    // todo
                    },
                    onResizeEnd: { left, right in
                    // todo
                        false
                    }
                )
            }
            
            ForEach(hoverButtonsUi, id: \.id) { buttonUi in
                ButtonView(
                    buttonUi: buttonUi,
                    extraRightWidth: .constant(0),
                    extraLeftWidth: .constant(0),
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
    @Binding var extraRightWidth: CGFloat
    @Binding var extraLeftWidth: CGFloat
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
            width: CGFloat(buttonUi.fullWidth) + extraRightWidth + extraLeftWidth,
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
    
    @GestureState private var dragLocationState = CGPoint(x: 0, y: 0)
    @State private var dragging: Bool = false
    
    @State private var localOffset = CGPoint(x: 0, y: 0)
    private var globalOffset: CGPoint {
        CGPoint(
            x: localOffset.x + CGFloat(buttonUi.initX),
            y: localOffset.y + CGFloat(buttonUi.initY)
        )
    }
    
    @State private var resizeOffsetLeft: CGFloat = 0

    var body: some View {
        ZStack {
            ButtonView(
                buttonUi: buttonUi,
                extraRightWidth: .constant(0),
                extraLeftWidth: $resizeOffsetLeft,
                content: {
                    HStack {
                        Circle()
                            .fill(.white)
                            .frame(width: barHeight, height: barHeight)
                            .gesture(
                                DragGesture(coordinateSpace: .global)
                                    .onChanged { value in
                                        resizeOffsetLeft = value.translation.width * -1
                                        onResize(resizeOffsetLeft, 0)
                                    }
                                    .onEnded { _ in
                                        let isPositionChanged = onResizeEnd(resizeOffsetLeft, 0)
                                        if !isPositionChanged {
                                            withAnimation {
                                                resizeOffsetLeft = 0
                                            }
                                        }
                                    }
                            )
                        Spacer()
                    }
                }
            )
        }
        .offset(x: localOffset.x, y: localOffset.y)
        .zIndex(dragging ? 2 : 1)
        .gesture(
            DragGesture()
                .updating($dragLocationState) { currentState, gestureState, transaction in
                    dragging = true
                    localOffset = CGPoint(
                        x: currentState.location.x - currentState.startLocation.x,
                        y: currentState.location.y - currentState.startLocation.y
                    )
                    onDrag(globalOffset)
                }
                .onEnded { _ in
                    dragging = false
                    let isPositionChanged = onDragEnd(globalOffset)
                    if !isPositionChanged {
                        withAnimation {
                            localOffset = CGPoint(x: 0, y: 0)
                        }
                    }
                }
        )
    }
}

private func calcCellWidth() -> CGFloat {
    let cellsCount: Int = HomeSettingsVm.companion.cellsCount.toInt()
    let width: CGFloat = UIScreen.main.bounds.size.width - (buttonsHPadding * 2)
    return (width - (spacing * CGFloat(cellsCount - 1))) / CGFloat(cellsCount)
}
