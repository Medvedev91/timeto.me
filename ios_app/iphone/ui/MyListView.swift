import SwiftUI
import shared

private let itemMinHeight = 45.0
private let paddingSectionSection: CGFloat = itemMinHeight.goldenRatioDown()

///
/// Paddings

struct MyListView__PaddingFirst: View {

    var body: some View {
        ZStack {
        }
        .frame(height: 14)
    }
}


struct MyListView__Padding__SectionSection: View {

    var body: some View {
        ZStack {
        }
        .frame(height: paddingSectionSection)
    }
}

struct MyListView__Padding__SectionHeader: View {

    var extraHeight = 0.0

    var body: some View {
        ZStack {
        }
        .frame(height: 30.0 + extraHeight)
    }
}

struct MyListView__Padding__HeaderSection: View {

    var body: some View {
        ZStack {
        }
        .frame(height: 6)
    }
}

///
/// Header

struct MyListView__HeaderView: View {

    let title: String
    var rightView: AnyView? = nil

    var body: some View {

        HStack {

            Text(title)
                .foregroundColor(.primary.opacity(0.55))
                .fontWeight(.regular)
                .font(.system(size: 13.5))

            Spacer()

            if let rightView = rightView {
                rightView
            }
        }
        .padding(.horizontal, H_PADDING + H_PADDING)
    }
}

///
/// Item

struct MyListView__ItemView<Content: View>: View {

    var isFirst: Bool
    var isLast: Bool

    var bgColor: Color = c.sheetFg

    var withTopDivider: Bool = false
    var dividerPaddingStart = H_PADDING

    var outerPaddingStart = H_PADDING
    var outerPaddingEnd = H_PADDING

    @ViewBuilder var content: () -> Content

    var body: some View {

        ZStack(alignment: .top) {

            content()
                .frame(maxWidth: .infinity)

            if withTopDivider {
                DividerFg()
                    .padding(.leading, dividerPaddingStart)
            }
        }
        .background(bgColor)
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
            itemMinHeight: itemMinHeight,
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
                    .padding(.trailing, H_PADDING - 2)
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

        MyListView__Item__Button(
            text: text,
            rightView: {
                Toggle("", isOn: $isActive)
                    .padding(.top, halfDpFloor)
                    .padding(.trailing, H_PADDING + 1)
                    .labelsHidden()
                    .onChange(of: isActiveState) { newValue in
                        isActive = newValue
                    }
            }
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
                    .padding(.trailing, H_PADDING)
            )
        ) {
            onClick()
        }
    }
}

///
/// Button

struct MyListView__Item__Button<Content: View>: View {

    let text: String
    var maxLines: Int = Int.max
    @ViewBuilder var rightView: () -> Content
    let onClick: () -> Void

    var body: some View {

        Button(
            action: {
                onClick()
            },
            label: {

                HStack {

                    Text(text)
                        .foregroundColor(c.text)
                        .lineLimit(maxLines)
                        .padding(.leading, H_PADDING)

                    Spacer()

                    rightView()
                }
                .frame(maxWidth: .infinity)
                .padding(.bottom, halfDpFloor)
                .frame(minHeight: itemMinHeight)
            }
        )
    }
}

// todo remove
struct MyListView__ItemView__ButtonView: View {

    let text: String
    var withArrow: Bool = false
    var maxLines: Int = Int.max
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
                        .padding(.leading, H_PADDING)
                        .lineLimit(maxLines)

                    Spacer()

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
                .padding(.bottom, halfDpFloor)
                .frame(minHeight: itemMinHeight)
            }
        )
        .foregroundColor(.primary)
    }
}

struct MyListView__ItemView__ActionView: View {

    let text: String
    var textColor: ColorRgba = .companion.red
    let onClick: () -> Void

    var body: some View {

        Button(
            action: {
                onClick()
            },
            label: {

                HStack {

                    Spacer()

                    Text(text)
                        .foregroundColor(textColor.toColor())
                        .fontWeight(.medium)

                    Spacer()
                }
                .frame(maxWidth: .infinity)
                .frame(minHeight: itemMinHeight)
            }
        )
        .foregroundColor(.primary)
    }
}

struct MyListView__Item__Button__RightText: View {

    let text: String
    var color: Color? = nil
    var paddingEndExtra: CGFloat = 0
    var fontSize: CGFloat = 17

    var body: some View {

        HStack {

            Text(text)
                .foregroundColor(color ?? c.tertiaryText)
                .font(.system(size: fontSize))
                .padding(.leading, 10)
                .padding(.trailing, 8 + halfDpFloor + paddingEndExtra)
                .lineLimit(1)

            MyListView__Item__Button__RightArrow()
        }
    }
}

// todo remove
struct MyListView__ItemView__ButtonView__RightText: View {

    let text: String
    var paddingEnd = H_PADDING
    var textColor: Color? = nil

    var body: some View {
        Text(text)
            .foregroundColor(textColor ?? c.tertiaryText)
            .padding(.leading, 10)
            .padding(.trailing, 10)
            .lineLimit(1)
    }
}

struct MyListView__Item__Button__RightArrow: View {

    var body: some View {
        Image(systemName: "chevron.right")
            .offset(y: onePx)
            .foregroundColor(c.tertiaryText)
            .font(.system(size: 14, weight: .medium))
            .padding(.trailing, H_PADDING - 1)
    }
}
