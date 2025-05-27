import SwiftUI
import shared

struct ColorPickerCustomSheet: View {
    
    init(
        initColorRgba: ColorRgba,
        onDone: @escaping (ColorRgba) -> Void
    ) {
        self.onDone = onDone
        _colorRgb = State(initialValue: ColorRgbLocal(
            r: initColorRgba.r.toDouble(),
            g: initColorRgba.g.toDouble(),
            b: initColorRgba.b.toDouble()
        ))
    }
    
    ///
    
    @State private var colorRgb: ColorRgbLocal
    private let onDone: (ColorRgba) -> Void
    
    @Environment(\.dismiss) private var dismiss
    
    private var colorRgba: ColorRgba {
        ColorRgba(r: Int32(colorRgb.r), g: Int32(colorRgb.g), b: Int32(colorRgb.b), a: 255)
    }
    
    var body: some View {
        VStack(alignment: .center) {
            Spacer()
            ColorSliderView(color: .red, value: $colorRgb.r)
            ColorSliderView(color: .green, value: $colorRgb.g)
            ColorSliderView(color: .blue, value: $colorRgb.b)
            Spacer()
        }
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button("Done") {
                    onDone(colorRgba)
                    dismiss()
                }
                .fontWeight(.semibold)
            }
            ToolbarItem(placement: .principal) {
                Text(ColorPickerVm.companion.prepCustomColorRgbaText(colorRgba: colorRgba))
                    .font(.system(size: 16))
                    .foregroundColor(.white)
                    .lineLimit(1)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 6)
                    .background(
                        RoundedRectangle(cornerRadius: 8, style: .continuous)
                            .fill(colorRgba.toColor())
                    )
            }
        }
        .onChange(of: colorRgba) { old, new in
            if old != new {
                Haptic.softShot()
            }
        }
        .toolbarTitleDisplayMode(.inline)
        .presentationDetents([.height(220)])
        .interactiveDismissDisabled()
    }
}

///

private struct ColorRgbLocal {
    var r: Double
    var g: Double
    var b: Double
}

private struct ColorSliderView: View {
    
    let color: Color
    @Binding var value: Double
    
    var body: some View {
        Slider(value: $value, in: 0...255)
            .accentColor(color)
            .padding(.horizontal, H_PADDING)
            .padding(.vertical, 6)
    }
}
