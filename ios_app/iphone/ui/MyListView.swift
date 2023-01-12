import SwiftUI

class MyListView {

    static let PADDING_SHEET_FIRST_HEADER = 30.0
    static let PADDING_HEADER_SECTION = 6.0
    static let PADDING_SECTION_HEADER = 38.0
    static let PADDING_SECTION_SECTION = 34.0

    static let PADDING_SECTION_OUTER_HORIZONTAL = 20.0
    static let PADDING_SECTION_ITEM_INNER_HORIZONTAL = 16.0

    static let ITEM_MIN_HEIGHT = 44.0
}

///
/// MyListView__HeaderView

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
                .padding(.leading, MyListView.PADDING_SECTION_OUTER_HORIZONTAL + 16)
    }
}

///
/// MyListView__SectionView

struct MyListView__SectionView<Content: View>: View {

    var paddingStart = MyListView.PADDING_SECTION_OUTER_HORIZONTAL
    var paddingEnd = MyListView.PADDING_SECTION_OUTER_HORIZONTAL
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
                MyDivider(xOffset: MyListView.PADDING_SECTION_ITEM_INNER_HORIZONTAL)
            }
        }
    }
}

struct MyListView__SectionView__TextInputView: View {

    @FocusState private var focusedField: Bool
    @State private var text: String

    // TRICK
    // Otherwise on init() with new text @State text would not updated.
    // It is needed for view model if input hints exists.
    private let stateText: String

    private let onValueChanged: (String) -> Void
    private let placeholder: String
    private let isAutofocus: Bool

    init(
            text: String,
            placeholder: String,
            isAutofocus: Bool,
            onValueChanged: @escaping (String) -> Void
    ) {
        _text = State(initialValue: text)
        stateText = text
        self.placeholder = placeholder
        self.isAutofocus = isAutofocus
        self.onValueChanged = onValueChanged
    }

    var body: some View {

        ZStack(alignment: .trailing) {

            ZStack {
                if #available(iOS 16.0, *) {
                    TextField(
                            text: $text,
                            prompt: Text(placeholder),
                            axis: .vertical
                    ) {
                        // todo what is it?
                    }
                            .padding(.vertical, 8)
                } else {
                    // One line ;(
                    TextField(text: $text, prompt: Text(placeholder)) {}
                }
            }
                    ///
                    .onChange(of: text) { newValue in
                        onValueChanged(newValue)
                    }
                    .onChange(of: stateText) { newValue in
                        text = newValue
                    }
                    ///
                    .focused($focusedField)
                    .textFieldStyle(.plain)
                    .frame(minHeight: MyListView.ITEM_MIN_HEIGHT)
                    .padding(.leading, MyListView.PADDING_SECTION_ITEM_INNER_HORIZONTAL)
                    .padding(.trailing, MyListView.PADDING_SECTION_ITEM_INNER_HORIZONTAL + 16) // for clear button

            TextFieldClearButtonView(
                    text: $text,
                    trailingPadding: 8
            ) {
                focusedField = true
            }
        }
                .onTapGesture {
                    focusedField = true
                }
                .onAppear {
                    if isAutofocus {
                        for i in 0...10 {
                            myAsyncAfter(0.1 * i.toDouble()) {
                                focusedField = true
                            }
                        }
                    }
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
                                    .padding(.leading, MyListView.PADDING_SECTION_ITEM_INNER_HORIZONTAL)

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
                                .padding(.trailing, MyListView.PADDING_SECTION_ITEM_INNER_HORIZONTAL - 2)
                )
        ) {
            onClick()
        }
    }
}
