import SwiftUI
import shared

struct ColorPickerSheet: View {
    
    let title: String
    let examplesData: ColorPickerExamplesData
    let onPick: (ColorRgba) -> Void
    
    var body: some View {
        VmView({
            ColorPickerVm(
                examplesData: examplesData
            )
        }) { vm, state in
            ColorPickerSheetInner(
                vm: vm,
                state: state,
                title: title,
                onPick: onPick
            )
        }
    }
}

///

private let circleSize: CGFloat = 42
private let circlePadding: CGFloat = 4
private let circleCellSize: CGFloat = circleSize + (circlePadding * 2.0)
private let exampleShape = RoundedRectangle(cornerRadius: 10, style: .continuous)
private let dividerPadding: CGFloat = H_PADDING.goldenRatioDown()

private struct ColorPickerSheetInner: View {
    
    let vm: ColorPickerVm
    let state: ColorPickerVm.State
    
    let title: String
    let onPick: (ColorRgba) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @State private var isRgbSlidersShowed = false

    var body: some View {
        
        VStack {
            
            HStack {
                
                ScrollView(showsIndicators: false) {
                    
                    HStack {
                        
                        VStack(alignment: .leading) {
                            
                            Text(state.examplesData.mainExample.title)
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundColor(.white)
                                .lineLimit(1)
                                .padding(.horizontal, 12)
                                .frame(height: circleSize - 2)
                                .background(exampleShape.fill(state.colorRgba.toColor()))
                                .padding(.top, circlePadding)
                            
                            let secondaryExamples = state.examplesData.secondaryExamples
                            if !secondaryExamples.isEmpty {
                                
                                Text(state.examplesData.secondaryHeader)
                                    .foregroundColor(.secondary)
                                    .fontWeight(.medium)
                                    .font(.system(size: 13))
                                    .padding(.leading, 4)
                                    .padding(.top, 28)
                                
                                ForEach(secondaryExamples, id: \.self) { exampleData in
                                    Button(
                                        action: {
                                            setColorRgbaLocal(exampleData.colorRgba)
                                        },
                                        label: {
                                            Text(exampleData.title)
                                                .font(.system(size: 15, weight: .semibold))
                                                .foregroundColor(.white)
                                                .lineLimit(1)
                                                .padding(.horizontal, 12)
                                                .padding(.top, 6)
                                                .padding(.bottom, 6)
                                                .background(exampleShape.fill(exampleData.colorRgba.toColor()))
                                                .padding(.top, 8)
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer()
                        
                        ZStack {}
                            .frame(width: onePx)
                            .frame(maxHeight: .infinity)
                            .background(.separator)
                            .padding(.top, circlePadding)
                    }
                }
                .contentMargins(.bottom, 12)
                .padding(.leading, H_PADDING)
                .clipped()

                ScrollView(showsIndicators: false) {
                    
                    VStack(alignment: .leading) {
                        
                        ForEach(state.colorGroups, id: \.self) { colors in
                            HStack {
                                ForEach(colors, id: \.self) { colorItem in
                                    ColorCircleView(colorItem: colorItem) {
                                        setColorRgbaLocal(colorItem.colorRgba)
                                    }
                                }
                            }
                        }
                        
                        Button("Custom") {
                            withAnimation {
                                isRgbSlidersShowed.toggle()
                            }
                        }
                        .padding(.top, 8)
                    }
                    .padding(.leading, dividerPadding - circlePadding)
                    .padding(.trailing, H_PADDING - circlePadding)
                    .padding(.bottom, 16)
                }
            }
            
            ZStack {
                
                if isRgbSlidersShowed {
                    
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
                                        .fill(state.colorRgba.toColor())
                                )
                        }
                        .padding(.top, 12)
                        .padding(.bottom, 8)
                        
                        ColorSliderView(value: Double(state.colorRgba.r), color: .red) { newValue in
                            setColorRgbaLocal(ColorRgba(r: Int(newValue).toInt32(), g: state.colorRgba.g, b: state.colorRgba.b, a: 255))
                        }
                        ColorSliderView(value: Double(state.colorRgba.g), color: .green) { newValue in
                            setColorRgbaLocal(ColorRgba(r: state.colorRgba.r, g: Int(newValue).toInt32(), b: state.colorRgba.b, a: 255))
                        }
                        ColorSliderView(value: Double(state.colorRgba.b), color: .blue) { newValue in
                            setColorRgbaLocal(ColorRgba(r: state.colorRgba.r, g: state.colorRgba.g, b: Int(newValue).toInt32(), a: 255))
                        }
                        .padding(.bottom, 8)
                    }
                    .fillMaxWidth()
                    .background(.black)
                    .transition(.move(edge: .bottom))
                } else {
                    // Otherwise vertical scale animation
                    Color.clear.frame(height: 0)
                }
            }
            .clipped()
        }
        .contentMargins(.top, 8)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(state.saveText) {
                    onPick(state.colorRgba)
                    dismiss()
                }
                .fontWeight(.semibold)
            }
        }
        .interactiveDismissDisabled()
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(title)
    }
    
    private func setColorRgbaLocal(_ colorRgba: ColorRgba) {
        vm.setColorRgba(colorRgba: colorRgba)
        if colorRgba != state.colorRgba {
            Haptic.softShot()
        }
    }
}

private struct ColorCircleView: View {
    
    let colorItem: ColorPickerVm.ColorItem
    let onClick: () -> Void
    
    ///
    
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
    
    private let valueVm: Double
    @State private var value: Double = 0
    private let color: Color
    private let onChange: (Double) -> Void
    
    init(
        value: Double,
        color: Color,
        onChange: @escaping (Double) -> Void
    ) {
        _value = State(initialValue: value)
        valueVm = value
        self.color = color
        self.onChange = onChange
    }
    
    var body: some View {
        Slider(value: $value, in: 0...255)
            .onChange(of: value) { _, newValue in
                onChange(newValue)
            }
            .animateVmValue(value: valueVm, state: $value)
            .accentColor(color)
            .padding(.horizontal, H_PADDING)
            .padding(.vertical, 6)
    }
}
