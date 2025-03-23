import SwiftUI
import shared

struct ColorPickerSheet: View {
    
    let title: String
    let initColorRgba: ColorRgba
    let onPick: (ColorRgba) -> Void
    
    var body: some View {
        VmView({
            ColorPickerVm(
                initColorRgba: initColorRgba
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

private struct ColorPickerSheetInner: View {
    
    let vm: ColorPickerVm
    let state: ColorPickerVm.State
    
    let title: String
    let onPick: (ColorRgba) -> Void
    
    ///
    
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        VStack {
        }
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
