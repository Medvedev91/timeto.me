import SwiftUI
import shared

private let rowHeight: CGFloat = HomeScreen__itemHeight
private let barHeight: CGFloat = HomeScreen__itemCircleHeight
private let spacing: CGFloat = 8.0

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
                    buttonUi: buttonUi
                )
            }
            
            ForEach(state.buttonsData.dataButtonsUi, id: \.id) { buttonUi in
                DragButtonView(
                    buttonUi: buttonUi,
                    onDragMove: { cgPoint in
                        hoverButtonsUi = vm.calcHoverButtonsUi(
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
                    }
                )
            }
            
            ForEach(hoverButtonsUi, id: \.id) { buttonUi in
                ButtonView(
                    buttonUi: buttonUi
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

private struct ButtonView: View {
    
    let buttonUi: HomeSettingsButtonUi
    
    private var offset: CGPoint {
        CGPoint(x: CGFloat(buttonUi.initX), y: CGFloat(buttonUi.initY))
    }
    
    var body: some View {
        ZStack {}
            .fillMaxWidth()
            .frame(height: barHeight)
            .background(roundedShape.fill(buttonUi.colorRgba.toColor()))
            .offset(x: offset.x, y: offset.y)
            .frame(
                width: CGFloat(buttonUi.fullWidth),
                height: rowHeight
            )
    }
}

private struct DragButtonView: View {
    
    let buttonUi: HomeSettingsButtonUi
    
    let onDragMove: (CGPoint) -> Void
    let onDragEnd: (CGPoint) -> Bool
    
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
    
    var body: some View {
        ButtonView(
            buttonUi: buttonUi
        )
        .offset(x: localOffset.x, y: localOffset.y)
        .zIndex(dragging ? 2 : 1)
        .gesture(
            DragGesture()
                .updating($dragLocationState) { currentState, gestureState, transaction in
                    dragging = true
                    // todo remove?
                    gestureState = currentState.location
                    localOffset = CGPoint(
                        x: gestureState.x - currentState.startLocation.x,
                        y: gestureState.y - currentState.startLocation.y
                    )
                    onDragMove(globalOffset)
                }
                .onEnded { _ in
                    dragging = false
                    let isPositionChanged = onDragEnd(globalOffset)
                    if !isPositionChanged {
                        localOffset = CGPoint(x: 0, y: 0)
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
