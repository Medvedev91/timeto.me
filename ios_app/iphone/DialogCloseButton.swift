import SwiftUI

struct DialogCloseButton: View {

    static let DEF_TRAILING = 50.0

    @Binding var isPresented: Bool
    var trailing = DEF_TRAILING
    var bottom = 8.0
    var withSaveArea = true
    var bgColor = Color(.systemBackground)

    private let size = 35.0

    @Environment(\.safeAreaInsets) private var safeAreaInsets

    var body: some View {

        Button(
                action: {
                    isPresented = false
                },
                label: {
                    Image(systemName: "xmark")
                            .foregroundColor(.secondary)
                            .frame(width: size, height: size)
                }
        )
                .background(roundedShape.fill(bgColor))
                .padding(.trailing, trailing)
                .padding(.bottom, bottom)
                .padding(withSaveArea ? safeAreaInsets : EdgeInsets())
    }
}
