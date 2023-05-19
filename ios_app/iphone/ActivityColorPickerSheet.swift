import SwiftUI
import shared

private let circleSize = 42.0
private let circlePadding = 4.0
private let circleCellSize = circleSize + (circlePadding * 2.0)
private let sheetHPaddings = MyListView.PADDING_OUTER_HORIZONTAL
private let dividerPadding = sheetHPaddings.goldenRatioDown()

struct ActivityColorPickerSheet: View {

    @State private var vm: ActivityColorPickerSheetVM
    @Binding private var isPresented: Bool
    private let onPick: (ColorRgba) -> Void

    @State private var circlesScroll = 0
    @State private var activitiesScroll = 0

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
                    scrollToHeader: circlesScroll
            ) {
                onPick(state.selectedColor)
                isPresented = false
            }

            HStack(spacing: 0) {

                ScrollViewWithVListener(showsIndicators: false, vScroll: $activitiesScroll) {

                    Row {

                        Column {

                            Padding(vertical: circlePadding)

                            Column {

                                Spacer()

                                Text(state.title)
                                        .font(.system(size: 17, weight: .medium))
                                        .foregroundColor(.white)
                                        .lineLimit(1)
                                        .padding(.leading, 11)
                                        .padding(.trailing, 13)
                                        .padding(.vertical, 8)
                                        .background(
                                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                                        .fill(state.selectedColor.toColor())
                                        )

                                Spacer()
                            }
                                    .frame(height: circleSize)

                            Text("todo")
                            Text("todo 2")
                        }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.leading, sheetHPaddings)
                                .padding(.trailing, dividerPadding)

                        ZStack {}
                                .frame(width: onePx)
                                .frame(maxHeight: .infinity)
                                .background(Color(.systemGray4))
                                .padding(.top, circlePadding)
                    }
                }

                ScrollViewWithVListener(showsIndicators: false, vScroll: $circlesScroll) {

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
                            .padding(.leading, dividerPadding - circlePadding)
                            .padding(.trailing, sheetHPaddings - circlePadding)
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
