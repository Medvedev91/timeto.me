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

///
/// Sheet__BottomView

struct Sheet__BottomView<Content: View>: View {

    @ViewBuilder let content: () -> Content

    var body: some View {

        VStack {
            DividerBg()
            content()
        }
                .background(Color(.bg))
    }
}

struct Sheet__BottomViewDefault<C1, C2>: View where C1: View, C2: View {

    let primaryText: String
    let primaryAction: () -> Void
    let secondaryText: String
    let secondaryAction: () -> Void
    var topContent: (() -> C1)? = nil
    var startContent: (() -> C2)? = nil

    var body: some View {

        Sheet__BottomView {
            if let topContent = topContent {
                topContent()
            }
            HStack(alignment: .center) {
                if let startContent = startContent {
                    startContent()
                }
                Spacer()
                HStack {
                    Sheet__BottomView__SecondaryButton(text: secondaryText) {
                        secondaryAction()
                    }
                    Sheet__BottomView__PrimaryButton(text: primaryText) {
                        primaryAction()
                    }
                }
                        .padding(.top, 10)
                        .padding(.trailing, MyListView.PADDING_OUTER_HORIZONTAL)
                        .padding(.bottom, 10)
            }
        }
    }
}

struct Sheet__BottomViewClose: View {

    let closeText = "Close"
    let onClick: () -> Void

    var body: some View {

        Sheet__BottomView {

            HStack {

                Spacer()

                Sheet__BottomView__SecondaryButton(text: closeText) {
                    onClick()
                }
            }
                    .padding(.top, 10)
                    .padding(.trailing, MyListView.PADDING_OUTER_HORIZONTAL)
                    .padding(.bottom, 10)
        }
    }
}

struct Sheet__BottomView__Button: View {

    let text: String
    let backgroundColor: Color
    let fontColor: Color
    let fontWeight: Font.Weight
    let onClick: () -> Void

    var body: some View {

        Button(
                action: {
                    onClick()
                },
                label: {
                    Text(text)
                            .padding(.horizontal, 13)
                            .padding(.top, 7)
                            .padding(.bottom, 8)
                            .foregroundColor(fontColor)
                            .font(.system(size: 16, weight: fontWeight))
                }
        )
                .background(roundedShape.fill(backgroundColor))
    }
}

struct Sheet__BottomView__PrimaryButton: View {

    let text: String
    let onClick: () -> Void

    var body: some View {
        Sheet__BottomView__Button(
                text: text,
                backgroundColor: .blue,
                fontColor: .white,
                fontWeight: .semibold
        ) {
            onClick()
        }
    }
}

struct Sheet__BottomView__SecondaryButton: View {

    let text: String
    let withPaddingRight: Bool = true
    let onClick: () -> Void

    var body: some View {
        Sheet__BottomView__Button(
                text: text,
                backgroundColor: .clear,
                fontColor: .secondary,
                fontWeight: .light
        ) {
            onClick()
        }
                .padding(.trailing, withPaddingRight ? 6.0 : 0.0)
    }
}
