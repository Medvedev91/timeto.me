import SwiftUI
import shared

struct ColorPickerSheet: View {
    
    let title: String
    let examplesUi: ColorPickerExamplesUi
    let onDone: (ColorRgba) -> Void
    
    var body: some View {
        VmView({
            ColorPickerVm(
                examplesUi: examplesUi
            )
        }) { vm, state in
            ColorPickerSheetInner(
                vm: vm,
                state: state,
                title: title,
                examplesUi: examplesUi,
                onDone: onDone
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
    let examplesUi: ColorPickerExamplesUi
    let onDone: (ColorRgba) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    @Environment(Navigation.self) private var navigation

    var body: some View {
        
        VStack {
            
            HStack {
                
                ScrollView(showsIndicators: false) {
                    
                    HStack {
                        
                        VStack(alignment: .leading) {
                            
                            Text(examplesUi.mainExampleUi.title)
                                .font(.system(size: 17, weight: .semibold))
                                .foregroundColor(.white)
                                .lineLimit(1)
                                .padding(.horizontal, 12)
                                .frame(height: circleSize - 2)
                                .background(exampleShape.fill(state.colorRgba.toColor()))
                                .padding(.top, circlePadding)
                            
                            let secondaryExamplesUi = examplesUi.secondaryExamplesUi
                            if !secondaryExamplesUi.isEmpty {
                                
                                Text(examplesUi.secondaryHeader)
                                    .foregroundColor(.secondary)
                                    .fontWeight(.medium)
                                    .font(.system(size: 13))
                                    .padding(.leading, 4)
                                    .padding(.top, 28)
                                
                                ForEach(secondaryExamplesUi, id: \.self) { exampleUi in
                                    Button(
                                        action: {
                                            setColorRgbaLocal(exampleUi.colorRgba)
                                        },
                                        label: {
                                            Text(exampleUi.title)
                                                .font(.system(size: 15, weight: .semibold))
                                                .foregroundColor(.white)
                                                .lineLimit(1)
                                                .padding(.horizontal, 12)
                                                .padding(.top, 6)
                                                .padding(.bottom, 6)
                                                .background(exampleShape.fill(exampleUi.colorRgba.toColor()))
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
                    }
                    .padding(.leading, dividerPadding - circlePadding)
                    .padding(.trailing, H_PADDING - circlePadding)
                    .padding(.bottom, 16)
                }
            }
        }
        .contentMargins(.top, 8)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") {
                    dismiss()
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(state.doneText) {
                    onDone(state.colorRgba)
                    dismiss()
                }
                .fontWeight(.semibold)
            }
            ToolbarItemGroup(placement: .bottomBar) {
                Spacer()
                Button("Custom Color") {
                    navigation.sheet {
                        ColorPickerCustomSheet(
                            initColorRgba: state.colorRgba,
                            onDone: { newColorRgba in
                                setColorRgbaLocal(newColorRgba)
                            }
                        )
                    }
                }
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
        .animateVmValue(vmValue: colorItem.isSelected, swiftState: $isSelectedAnim)
    }
}
