import SwiftUI
import shared

struct ColorPickerSheet: View {

    @State private var vm: ColorPickerSheetVM
    @Binding private var isPresented: Bool

    private let selectedColor: ColorRgba
    private let text: String
    private let onPick: (ColorRgba) -> Void

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

        }
                .background(Color(.mySheetFormBg))
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
