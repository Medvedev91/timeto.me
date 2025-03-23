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

private struct ColorPickerSheetInner: View {
    
    let vm: ColorPickerVm
    let state: ColorPickerVm.State
    
    let title: String
    let onPick: (ColorRgba) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        
        VStack {
            
            HStack {
                
                ScrollView(showsIndicators: false) {
                    
                    VStack(alignment: .leading) {
                        
                        Text(state.examplesData.mainExample.title)
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(.white)
                            .lineLimit(1)
                            .padding(.horizontal, 12)
                            .frame(height: circleSize - 2)
                            .background(exampleShape.fill(state.colorRgba.toColor()))
                            .padding(.top, 1)
                        
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
                                        vm.setColorRgba(colorRgba: exampleData.colorRgba)
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
                }
                .contentMargins(.bottom, 12)
                .padding(.leading, H_PADDING)
                
                Spacer()
            }
        }
        .myFormContentMargins()
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
        .toolbarTitleDisplayMode(.inline)
        .navigationTitle(title)
    }
}
