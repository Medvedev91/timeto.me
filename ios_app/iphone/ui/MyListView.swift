import SwiftUI
import shared

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

    var body: some View {

        ZStack(alignment: .top) {

            content()
                    .frame(maxWidth: .infinity)

            if withTopDivider {
                MyDivider(xOffset: dividerPaddingStart)
            }
        }
                .background(Color(.mySecondaryBackground))
                .cornerRadius(10, onTop: isFirst, onBottom: isLast)
                .padding(.leading, outerPaddingStart)
                .padding(.trailing, outerPaddingEnd)
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

struct MyListView__ItemView__RadioView: View {

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

struct MyListView__ItemView__SwitchView: View {

    @State private var isActive: Bool
    private let isActiveState: Bool

    private let text: String
    private let onClick: () -> Void

    init(
            text: String,
            isActive: Bool,
            onClick: @escaping () -> Void
    ) {
        self.text = text
        self.onClick = onClick

        _isActive = State(initialValue: isActive)
        isActiveState = isActive
    }

    var body: some View {

        MyListView__ItemView__ButtonView(
                text: text,
                rightView: AnyView(
                        Toggle("", isOn: $isActive)
                                .padding(.trailing, 10)
                                .labelsHidden()
                                .onChange(of: isActiveState) { newValue in
                                    isActive = newValue
                                }
                )
        ) {
            onClick()
        }
    }
}

struct MyListView__ItemView__CheckboxView: View {

    let text: String
    let isChecked: Bool
    let onClick: () -> Void

    var body: some View {

        MyListView__ItemView__ButtonView(
                text: text,
                rightView: AnyView(
                        Image(systemName: "checkmark")
                                .foregroundColor(isChecked ? .blue : .clear)
                                .padding(.trailing, MyListView.PADDING_INNER_HORIZONTAL)
                )
        ) {
            onClick()
        }
    }
}

///
/// Button

struct MyListView__ItemView__ButtonView: View {

    let text: String
    var withArrow: Bool = false
    var rightView: AnyView? = nil
    let onClick: () -> Void

    var body: some View {

        Button(
                action: {
                    onClick()
                },
                label: {

                    HStack(spacing: 0) {

                        Text(text)
                                .padding(.leading, MyListView.PADDING_INNER_HORIZONTAL)

                        Spacer(minLength: 0)

                        if let rightView = rightView {
                            rightView
                        }

                        if withArrow {
                            Image(systemName: "chevron.right")
                                    .foregroundColor(.secondary)
                                    .font(.system(size: 16, weight: .medium))
                                    .padding(.trailing, 12)
                        }
                    }
                            .frame(maxWidth: .infinity)
                            .frame(minHeight: MyListView.ITEM_MIN_HEIGHT)
                }
        )
                .foregroundColor(.primary)
    }
}

struct MyListView__ItemView__ActionView: View {

    let text: String
    var textColor: ColorNative = .red
    let onClick: () -> Void

    var body: some View {

        Button(
                action: {
                    onClick()
                },
                label: {

                    HStack(spacing: 0) {

                        Spacer(minLength: 0)

                        Text(text)
                                .foregroundColor(textColor.toColor())
                                .fontWeight(.medium)

                        Spacer(minLength: 0)
                    }
                            .frame(maxWidth: .infinity)
                            .frame(minHeight: MyListView.ITEM_MIN_HEIGHT)
                }
        )
                .foregroundColor(.primary)
    }
}

struct MyListView__ItemView__ButtonView__RightText: View {

    let text: String
    var paddingEnd = MyListView.PADDING_INNER_HORIZONTAL
    var textColor: Color? = nil

    var body: some View {
        Text(text)
                .foregroundColor(textColor ?? Color(.myFormButtonRightNoteText))
                .padding(.leading, 10)
                .padding(.trailing, 10)
                .lineLimit(1)
    }
}
