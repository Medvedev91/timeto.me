import SwiftUI

struct Sheet__HeaderView: View {

    let title: String
    let scrollToHeader: Int
    let bgColor: Color

    private var bgAlpha: Double {
        (Double(scrollToHeader) / 30).limitMinMax(0, 1)
    }

    var body: some View {

        ZStack(alignment: .bottom) {

            Text(title)
                    .font(.system(size: 22, weight: .semibold))
                    .multilineTextAlignment(.center)
                    .padding(.top, 18)
                    .padding(.bottom, 18)

            Color(.dividerBg)
                    .opacity(bgAlpha)
                    .frame(height: onePx)
        }
                .background(bgColor.opacity(bgAlpha))
    }
}
