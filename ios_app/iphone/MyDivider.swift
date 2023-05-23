import SwiftUI

struct MyDivider: View {

    var xOffset = 0.0

    var body: some View {
        Color(.systemGray4)
                .frame(height: onePx)
                .padding(.leading, xOffset)
    }
}
