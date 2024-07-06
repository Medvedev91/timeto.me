import SwiftUI

struct DividerBgScroll: View {

    let scrollToHeader: Int

    private var bgAlpha: Double {
        (Double(scrollToHeader) / 30).limitMinMax(0, 1)
    }

    var body: some View {
        DividerBg()
            .opacity(bgAlpha)
            .padding(.horizontal, H_PADDING)
    }
}
