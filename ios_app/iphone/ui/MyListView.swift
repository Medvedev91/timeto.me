import SwiftUI

class MyListView {

    static let ITEM_MIN_HEIGHT = 44.0

    static let PADDING_OUTER_HORIZONTAL = 20.0
    static let PADDING_INNER_HORIZONTAL = 16.0

    static let PADDING_HEADER_SECTION = 6.0
    static let PADDING_SECTION_HEADER = 38.0
    static let PADDING_SECTION_SECTION = 34.0
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
        ZStack {}.frame(height: 2)
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
/// MyListView__SectionView

struct MyListView__SectionView<Content: View>: View {

    var paddingStart = MyListView.PADDING_OUTER_HORIZONTAL
    var paddingEnd = MyListView.PADDING_OUTER_HORIZONTAL
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(spacing: 0) {
            content()
        }
                .background(Color(.mySecondaryBackground))
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                .padding(.leading, paddingStart)
                .padding(.trailing, paddingEnd)
    }
}

///
/// MyListView__SectionView__ItemView

struct MyListView__SectionView__ItemView<Content: View>: View {

    let withTopDivider: Bool
    @ViewBuilder var content: () -> Content

    var body: some View {

        ZStack(alignment: .top) {

            content()
                    .frame(maxWidth: .infinity)
                    .frame(minHeight: MyListView.ITEM_MIN_HEIGHT)

            if withTopDivider {
                MyDivider(xOffset: MyListView.PADDING_INNER_HORIZONTAL)
            }
        }
    }
}

struct MyListView__SectionView__TextInputView: View {

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

///
/// MyListView__SectionView__ButtonView

struct MyListView__SectionView__ButtonView: View {

    let text: String
    let withTopDivider: Bool
    var rightView: AnyView? = nil
    let onClick: () -> Void

    var body: some View {

        MyListView__SectionView__ItemView(
                withTopDivider: withTopDivider
        ) {
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
                    }
            )
                    .foregroundColor(.primary)
        }
    }
}

struct MyListView__SectionView__SwitcherView: View {

    let text: String
    let withTopDivider: Bool
    let isActive: Bool
    let onClick: () -> Void

    var body: some View {

        MyListView__SectionView__ButtonView(
                text: text,
                withTopDivider: withTopDivider,
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
