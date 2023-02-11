import SwiftUI

class MyListView {

    static let ITEM_MIN_HEIGHT = 44.0

    static let PADDING_OUTER_HORIZONTAL = 20.0
    static let PADDING_INNER_HORIZONTAL = 16.0
}

///
/// Paddings

struct MyListView__Padding__SectionSection: View {

    var body: some View {
        ZStack {}.frame(height: 34)
    }
}

struct MyListView__Padding__SectionHeader: View {

    var extraHeight = 0.0

    var body: some View {
        ZStack {}.frame(height: 30.0 + extraHeight)
    }
}

struct MyListView__Padding__HeaderSection: View {

    var body: some View {
        ZStack {}.frame(height: 6)
    }
}

///
/// Header

struct MyListView__HeaderView: View {

    let title: String
    var rightView: AnyView? = nil

    var body: some View {

        HStack(spacing: 0) {

            Text(title)
                    .foregroundColor(.primary.opacity(0.55))
                    .fontWeight(.regular)
                    .font(.system(size: 13.5))

            Spacer(minLength: 0)

            if let rightView = rightView {
                rightView
            }
        }
                .padding(.horizontal, MyListView.PADDING_OUTER_HORIZONTAL + MyListView.PADDING_INNER_HORIZONTAL)
    }
}

///
/// Item

struct MyListView__ItemView<Content: View>: View {

    var isFirst: Bool
    var isLast: Bool

    var withTopDivider: Bool = false
    var dividerPaddingStart = MyListView.PADDING_INNER_HORIZONTAL

    var outerPaddingStart = MyListView.PADDING_OUTER_HORIZONTAL
    var outerPaddingEnd = MyListView.PADDING_OUTER_HORIZONTAL

    @ViewBuilder var content: () -> Content

    private var corners: UIRectCorner {
        var corners: UIRectCorner = []
        if isFirst {
            corners.insert(.topLeft)
            corners.insert(.topRight)
        }
        if isLast {
            corners.insert(.bottomLeft)
            corners.insert(.bottomRight)
        }
        return corners
    }

    var body: some View {

        ZStack(alignment: .top) {

            content()
                    .frame(maxWidth: .infinity)

            if withTopDivider {
                MyDivider(xOffset: MyListView.PADDING_INNER_HORIZONTAL)
            }
        }
                .background(Color(.mySecondaryBackground))
                .timeToSheetCornerRadius(10, corners: corners)
                .padding(.leading, outerPaddingStart)
                .padding(.trailing, outerPaddingEnd)
    }
}

///
/// Button

struct MyListView__ItemView__ButtonView: View {

    let text: String
    var rightView: AnyView? = nil
    let onClick: () -> Void

    var body: some View {

        Button(
                action: {
                    onClick()
                },
                label: {

                    HStack {

                        Text(text)
                                .padding(.leading, MyListView.PADDING_INNER_HORIZONTAL)

                        Spacer(minLength: 0)

                        if let rightView = rightView {
                            rightView
                        }
                    }
                            .frame(maxWidth: .infinity)
                            .frame(minHeight: MyListView.ITEM_MIN_HEIGHT)
                }
        )
                .foregroundColor(.primary)
    }
}

struct MyListView__ItemView__SwitcherView: View {

    let text: String
    let isActive: Bool
    let onClick: () -> Void

    var body: some View {

        MyListView__ItemView__ButtonView(
                text: text,
                rightView: AnyView(
                        Image(systemName: isActive ? "circle.inset.filled" : "circle")
                                .foregroundColor(isActive ? .blue : .primary)
                                .padding(.trailing, MyListView.PADDING_INNER_HORIZONTAL - 2)
                )
        ) {
            onClick()
        }
    }
}

struct MyListView__ItemView__TextInputView: View {

    let text: String
    let placeholder: String
    let isAutofocus: Bool
    let onValueChanged: (String) -> Void

    @FocusState private var isFocused: Bool

    var body: some View {

        TextField__VMState(
                text: text,
                placeholder: placeholder,
                isFocused: $isFocused,
                onValueChanged: onValueChanged
        )
                .onAppear {
                    isFocused = isAutofocus
                }
    }
}
