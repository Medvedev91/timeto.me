import SwiftUI

struct MyDivider: View {

    var xOffset = 0.0

    var body: some View {
        Color(.systemGray4)
                .frame(height: 1 / UIScreen.main.scale)
                .offset(x: xOffset)
    }
}
