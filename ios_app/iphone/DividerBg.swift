import SwiftUI

struct DividerBg: View {

    var xOffset = 0.0

    var body: some View {
        Color(.dividerBg)
                .frame(height: onePx)
                .padding(.leading, xOffset)
    }
}
