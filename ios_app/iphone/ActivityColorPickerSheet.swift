import SwiftUI
import shared

private let circleSize = 42.0
private let circlePadding = 4.0
private let circleCellSize = circleSize + (circlePadding * 2.0)
private let sheetHPadding = MyListView.PADDING_OUTER_HORIZONTAL
private let dividerPadding = sheetHPadding.goldenRatioDown()

struct ActivityColorPickerSheet: View {

    @State private var vm: ActivityColorPickerSheetVM
    @Binding private var isPresented: Bool
    private let onPick: (ColorRgba) -> Void

    @State private var circlesScroll = 0
    @State private var activitiesScroll = 0

    @State private var isRgbSlidersShowed = false

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

        VMView(vm: vm, stack: .VStack()) { state in

            SheetHeaderView(
                    onCancel: { isPresented.toggle() },
                    title: state.headerTitle,
                    doneText: state.doneTitle,
                    isDoneEnabled: true,
                    scrollToHeader: circlesScroll + activitiesScroll
            ) {
                onPick(state.selectedColor)
                isPresented = false
            }

            VStack {

                HStack {

                    ScrollViewWithVListener(showsIndicators: false, vScroll: $activitiesScroll) {

                        HStack {

                            VStack(alignment: .leading) {

                                Text(state.title)
                                        .font(.system(size: 17, weight: .bold))
                                        .foregroundColor(.white)
                                        .lineLimit(1)
                                        .padding(.leading, 11)
                                        .padding(.trailing, 13)
                                        .frame(height: circleSize - 4)
                                        .background(
                                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                                        .fill(state.selectedColor.toColor())
                                        )
                                        .padding(.top, 2)

                                Text(state.otherActivitiesTitle)
                                        .foregroundColor(.secondary)
                                        .fontWeight(.medium)
                                        .font(.system(size: 13))
                                        .padding(.leading, 4)
                                        .padding(.top, 28)

                                ForEach(state.allActivities, id: \.self) { activityUI in
                                    Text(activityUI.text)
                                            .font(.system(size: 15, weight: .medium))
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
                                .padding(.top, circlePadding)
                                .safeAreaPadding(.bottom)
                    }

                    ScrollViewWithVListener(showsIndicators: false, vScroll: $circlesScroll) {

                        VStack(alignment: .leading) {

                            ForEach(state.colorGroups, id: \.self) { colors in

                                HStack {

                                    ForEach(colors, id: \.self) { colorItem in

                                        Button(
                                                action: {
                                                    vm.upColorRgba(colorRgba: colorItem.colorRgba)
                                                },
                                                label: {

                                                    ZStack {

                                                        Circle()
                                                                .foregroundColor(colorItem.colorRgba.toColor())
                                                                .frame(width: circleSize, height: circleSize)

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

                            Button(
                                    action: {
                                        vm.toggleIsRgbSlidersShowed()
                                    },
                                    label: { Text("Custom") }
                            )
                                    .padding(.top, 6)
                                    .padding(.bottom, 20)
                                    .padding(.leading, circlePadding + 1)
                                    .animateVmValue(value: state.isRgbSlidersShowed, state: $isRgbSlidersShowed)
                        }
                                .padding(.leading, dividerPadding - circlePadding)
                                .padding(.trailing, sheetHPadding - circlePadding)
                                .safeAreaPadding(.bottom)
                    }
                }

                if (isRgbSlidersShowed) {

                    VStack {

                        MyDivider()

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

                            HStack {
                                Spacer()
                                Button(
                                        action: { vm.toggleIsRgbSlidersShowed() },
                                        label: {
                                            Image(systemName: "chevron.down.circle.fill")
                                                    .font(.system(size: 24, weight: .medium))
                                                    .foregroundColor(Color(.iconButtonBgBg1))
                                        }
                                )
                            }
                                    .padding(.trailing, sheetHPadding + circlePadding + 2)
                        }
                                .padding(.top, 12)
                                .padding(.bottom, 8)

                        ColorSliderView(value: Double(state.r), color: .red) { vm.upR(r: Float($0)) }
                        ColorSliderView(value: Double(state.g), color: .green) { vm.upG(g: Float($0)) }
                        ColorSliderView(value: Double(state.b), color: .blue) { vm.upB(b: Float($0)) }
                    }
                            .safeAreaPadding(.bottom)
                            .background(Color(.mySecondaryBackground))
                            .transition(.move(edge: .bottom))
                }
            }
        }
                .ignoresSafeArea()
                .background(Color(.mySecondaryBackground))
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
