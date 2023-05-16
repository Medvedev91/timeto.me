import SwiftUI
import shared

struct ColorPickerSheet: View {

    @State private var vm: ColorPickerSheetVM
    @Binding private var isPresented: Bool

    private let selectedColor: ColorRgba
    private let text: String
    private let onPick: (ColorRgba) -> Void

    @State private var sheetHeaderScroll = 0

    init(
            isPresented: Binding<Bool>,
            selectedColor: ColorRgba,
            text: String,
            onPick: @escaping (ColorRgba) -> Void
    ) {
        vm = ColorPickerSheetVM(selectedColor: selectedColor, text: text)
        _isPresented = isPresented

        self.selectedColor = selectedColor
        self.text = text
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
                onPick(state.getSelectedColor())
                isPresented = false
            }

            ScrollViewWithVListener(showsIndicators: false, vScroll: $sheetHeaderScroll) {
                VStack(spacing: 0) {
                    ZStack {}.frame(height: 8)
                    ForEach(state.colorGroups, id: \.self) { colors in
                        HStack(spacing: 0) {
                            ForEach(colors, id: \.self) { color in
                                HStack(spacing: 0) {
                                    Spacer(minLength: 0)
                                    Button(
                                            action: {
                                                vm.upColorRgba(colorRgba: color.colorRgba)
                                            },
                                            label: {
                                                ZStack {
                                                    ColorPickerSheet__ColorCircleView(
                                                            color: color.colorRgba.toColor(),
                                                            size: 42
                                                    )
                                                }
                                                        .padding(.vertical, 4)
                                            }
                                    )
                                    Spacer(minLength: 0)
                                }
                            }
                        }
                    }
                    ZStack {}.frame(height: 8)
                }
                        .padding(.horizontal, 16)
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
