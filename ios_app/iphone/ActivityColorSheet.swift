import SwiftUI
import shared

private let circleSize = 42.0
private let circlePadding = 4.0
private let circleCellSize = circleSize + (circlePadding * 2.0)
private let sheetHPadding = MyListView.PADDING_OUTER_HORIZONTAL
private let dividerPadding = sheetHPadding.goldenRatioDown()
private let bgColor = Color(.bgSheet)

struct ActivityColorSheet: View {

    @State private var vm: ActivityColorSheetVM
    @Binding private var isPresented: Bool
    private let onPick: (ColorRgba) -> Void

    @State private var circlesScroll = 0
    @State private var activitiesScroll = 0

    @State private var isRgbSlidersShowedAnim = false

    init(
            isPresented: Binding<Bool>,
            initData: ActivityColorSheetVM.InitData,
            onPick: @escaping (ColorRgba) -> Void
    ) {
        vm = ActivityColorSheetVM(initData: initData)
        _isPresented = isPresented
        self.onPick = onPick
    }

    var body: some View {

        VMView(vm: vm, stack: .VStack()) { state in

            Sheet__HeaderView(
                    title: state.headerTitle,
                    scrollToHeader: (circlesScroll + activitiesScroll) * 4, // x4 speed uph
                    bgColor: bgColor
            )

            VStack {

                HStack {

                    ScrollViewWithVListener(showsIndicators: false, vScroll: $activitiesScroll) {

                        HStack {

                            VStack(alignment: .leading) {

                                Text(state.title)
                                        .font(.system(size: 17, weight: .bold))
                                        .foregroundColor(.white)
                                        .lineLimit(1)
                                        .padding(.leading, 12)
                                        .padding(.trailing, 14)
                                        .frame(height: circleSize - 2)
                                        .background(squircleShape.fill(state.selectedColor.toColor()))
                                        .padding(.top, 1)

                                Text(state.otherActivitiesTitle)
                                        .foregroundColor(state.otherActivitiesTitleColor.toColor())
                                        .fontWeight(.medium)
                                        .font(.system(size: 13))
                                        .padding(.leading, 4)
                                        .padding(.top, 28)

                                ForEach(state.allActivities, id: \.self) { activityUI in
                                    Button(
                                            action: {
                                                vm.upColorRgba(colorRgba: activityUI.colorRgba)
                                            },
                                            label: {
                                                Text(activityUI.text)
                                                        .font(.system(size: 15, weight: .semibold))
                                                        .foregroundColor(.white)
                                                        .lineLimit(1)
                                                        .padding(.leading, 9)
                                                        .padding(.trailing, 10)
                                                        .padding(.top, 6)
                                                        .padding(.bottom, 6)
                                                        .background(
                                                                RoundedRectangle(cornerRadius: 10, style: .continuous)
                                                                        .fill(activityUI.colorRgba.toColor())
                                                        )
                                                        .padding(.top, 8)
                                            }
                                    )
                                }

                                Padding(vertical: 20)
                            }
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(.leading, sheetHPadding)
                                    .padding(.trailing, dividerPadding)

                            ZStack {}
                                    .frame(width: onePx)
                                    .frame(maxHeight: .infinity)
                                    .background(Color(.systemGray4))
                                    .padding(.top, circlePadding)
                        }
                                .padding(.top, 4)
                                .safeAreaPadding(.bottom)
                    }

                    ScrollViewWithVListener(showsIndicators: false, vScroll: $circlesScroll) {

                        VStack(alignment: .leading) {

                            ForEachIndexedId(state.colorGroups) { _, colors in

                                HStack {

                                    ForEachIndexedId(colors) { _, colorItem in

                                        ColorCircleView(colorItem: colorItem) {
                                            vm.upColorRgba(colorRgba: colorItem.colorRgba)
                                        }
                                    }
                                }
                            }
                        }
                                .padding(.leading, dividerPadding - circlePadding)
                                .padding(.trailing, sheetHPadding - circlePadding)
                                .padding(.bottom, 16)
                    }
                }

                Sheet__BottomViewDefault(
                        primaryText: state.doneTitle,
                        primaryAction: {
                            onPick(state.selectedColor)
                            isPresented = false
                        },
                        secondaryText: "Cancel",
                        secondaryAction: {
                            isPresented = false
                        },
                        topContent: {

                            ZStack {

                                if (isRgbSlidersShowedAnim) {

                                    VStack {

                                        ZStack(alignment: .center) {

                                            Text(state.rgbText)
                                                    .font(.system(size: 16))
                                                    .foregroundColor(.white)
                                                    .lineLimit(1)
                                                    .padding(.horizontal, 8)
                                                    .padding(.vertical, 4)
                                                    .background(
                                                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                                                    .fill(state.selectedColor.toColor())
                                                    )
                                        }
                                                .padding(.top, 12)
                                                .padding(.bottom, 8)

                                        ColorSliderView(value: Double(state.r), color: .red) { vm.upR(r: Float($0)) }
                                        ColorSliderView(value: Double(state.g), color: .green) { vm.upG(g: Float($0)) }
                                        ColorSliderView(value: Double(state.b), color: .blue) { vm.upB(b: Float($0)) }
                                                .padding(.bottom, 8)
                                    }
                                            .background(c.bg)
                                            .transition(.move(edge: .bottom))
                                } else {
                                    // Otherwise vertical scale animation
                                    Color.clear.frame(height: 0)
                                }
                            }
                                    .clipped()
                        },
                        startContent: {

                            ZStack {

                                Button(
                                        action: {
                                            vm.toggleIsRgbSlidersShowed()
                                        },
                                        label: {
                                            ZStack {
                                                Image(systemName: isRgbSlidersShowedAnim ? "chevron.down" : "slider.horizontal.3")
                                                        .font(.system(size: isRgbSlidersShowedAnim ? 16 : 22, weight: .medium))
                                                        .foregroundColor(state.rgbSlidersBtnColor.toColor())
                                                        .offset(y: isRgbSlidersShowedAnim ? 1 : 0)
                                            }
                                                    .frame(width: 34, height: 34)
                                                    .background(roundedShape.fill(isRgbSlidersShowedAnim ? .blue : .clear))
                                        }
                                )
                            }
                                    .animateVmValue(value: state.isRgbSlidersShowed, state: $isRgbSlidersShowedAnim)
                                    .padding(.leading, sheetHPadding)
                        }
                )
                        .safeAreaPadding(.bottom)
            }
        }
                .ignoresSafeArea()
                .background(bgColor)
    }
}

private struct ColorCircleView: View {

    let colorItem: ActivityColorSheetVM.ColorItem
    let onClick: () -> Void

    @State private var isSelectedAnim = false

    var body: some View {

        Button(
                action: {
                    onClick()
                },
                label: {
                    ZStack {

                        Circle()
                                .foregroundColor(colorItem.colorRgba.toColor())
                                .frame(width: circleSize, height: circleSize)
                                .zIndex(1)

                        if isSelectedAnim {
                            Image(systemName: "checkmark")
                                    .font(.system(size: 18, weight: .medium))
                                    .foregroundColor(.white)
                                    .transition(.opacity)
                                    .zIndex(2)
                        }
                    }
                            .padding(.all, circlePadding)
                }
        )
                .frame(width: circleCellSize, height: circleCellSize)
                .animateVmValue(value: colorItem.isSelected, state: $isSelectedAnim)
    }
}

private struct ColorSliderView: View {

    private let valueVM: Double
    @State private var value: Double = 0
    private let color: Color
    private let onChange: (Double) -> Void

    init(
            value: Double,
            color: Color,
            onChange: @escaping (Double) -> Void
    ) {
        _value = State(initialValue: value)
        valueVM = value
        self.color = color
        self.onChange = onChange
    }

    var body: some View {
        Slider(value: $value, in: 0...255)
                ///
                .onChange(of: value) { newValue in
                    onChange(newValue)
                }
                .animateVmValue(value: valueVM, state: $value)
                ///
                .accentColor(color)
                .padding(.horizontal, sheetHPadding)
                .padding(.vertical, 6)
    }
}
