import SwiftUI

struct DividerBg: View {

    var isVisible = true

    var body: some View {
        Divider(color: isVisible ? c.dividerBg : .clear)
    }
}
