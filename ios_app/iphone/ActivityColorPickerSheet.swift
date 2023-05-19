import SwiftUI
import shared

private let circleSize = 42.0
private let circlePadding = 4.0
private let circleCellSize = circleSize + (circlePadding * 2.0)

struct ActivityColorPickerSheet: View {

    @State private var vm: ActivityColorPickerSheetVM
    @Binding private var isPresented: Bool
    private let onPick: (ColorRgba) -> Void

    @State private var sheetHeaderScroll = 0

    init(
            isPresented: Binding<Bool>,
            initData: ActivityColorPickerSheetVM.InitData,
            onPick: @escaping (ColorRgba) -> Void
    ) {
        vm = ActivityColorPickerSheetVM(initData: initData)
        _isPresented = isPresented
        self.onPick = onPick
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: state.doneTitle,
                    isDoneEnabled: true,
                    scrollToHeader: sheetHeaderScroll
            ) {
                onPick(state.selectedColor)
                isPresented = false
            }

            HStack(spacing: 0) {

                Spacer(minLength: 0)

                ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {

                    VStack(spacing: 0) {

                        ForEach(state.colorGroups, id: \.self) { colors in

                            HStack(spacing: 0) {

                                ForEach(colors, id: \.self) { colorItem in

                                    Button(
                                            action: {
                                                vm.upColorRgba(colorRgba: colorItem.colorRgba)
                                            },
                                            label: {

                                                ZStack {

                                                    ColorPickerSheet__ColorCircleView(
                                                            color: colorItem.colorRgba.toColor(),
                                                            size: circleSize
                                                    )

                                                    if colorItem.isSelected {
                                                        Image(systemName: "checkmark")
                                                                .font(.system(size: 18, weight: .medium))
                                                                .foregroundColor(.white)
                                                    }
                                                }
                                                        .padding(.all, circlePadding)
                                            }
                                    )
                                            .frame(width: circleCellSize, height: circleCellSize)
                                }
                            }
                        }

                        ZStack {}.frame(height: 8)
                    }
                            .padding(.horizontal, 16)
                }
            }
        }
                .background(Color.white)
    }
}

struct ColorPickerSheet__ColorCircleView: View {

    let color: Color
    let size: CGFloat

    var body: some View {
        Circle()
                .strokeBorder(Color(UIColor.lightGray), lineWidth: onePx)
                .frame(width: size, height: size)
                .background(Circle().foregroundColor(color))
    }
}
