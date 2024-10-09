import SwiftUI

let Fs__TITLE_FONT_SIZE = 27.0 // Golden ratio to lists text
let Fs__TITLE_FONT_WEIGHT: Font.Weight = .heavy
let Fs__BUTTON_FONT_SIZE = 16.0

class Fs: ObservableObject {

    @Published var items = [Fs__Item<AnyView>]()

    func show<Content: View>(
        @ViewBuilder content: @escaping (Binding<Bool>) -> Content
    ) {
        items.append(
            Fs__Item(
                content: { isPresented in
                    AnyView(content(isPresented))
                }
            )
        )
    }
}

extension View {

    func attachFs() -> some View {
        modifier(Fs__Modifier())
    }
}

private let fsAnimation = Animation.easeInOut(duration: 0.20)

///
///

struct Fs__Item<Content: View>: View, Identifiable {

    @ViewBuilder var content: (Binding<Bool>) -> Content

    @EnvironmentObject private var fs: Fs
    @State private var isPresented = false

    let id = UUID().uuidString

    var body: some View {

        ZStack {

            if isPresented {

                ZStack {

                    VStack {
                        HStack {
                            Spacer()
                        }
                        Spacer()
                    }
                    .background(c.bg)
                    .ignoresSafeArea()

                    content($isPresented)
                        .ignoresSafeArea(.container) // Keep keyboard's paddings
                        .onDisappear {
                            fs.items.removeAll {
                                $0.id == id
                            }
                        }
                }
                .transition(.opacity)
            }
        }
        .animation(fsAnimation, value: isPresented)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
        .onAppear {
            isPresented = true
            hideKeyboard()
        }
    }
}

private struct Fs__Modifier: ViewModifier {

    @StateObject private var fs = Fs()

    func body(content: Content) -> some View {
        ZStack {
            content
            ForEach(fs.items) { wrapper in
                wrapper
            }
        }
        .environmentObject(fs)
    }
}

///

struct Fs__CloseButton: View {

    let onClick: () -> Void

    var body: some View {

        Button(
            action: {
                onClick()
            },
            label: {
                Image(systemName: "xmark")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(c.tertiaryText)
            }
        )
        .frame(width: 32, height: 32)
        .background(roundedShape.fill(c.fg))
    }
}

struct Fs__Header<Content: View>: View {

    let scrollToHeader: Int
    @ViewBuilder var content: () -> Content

    var body: some View {

        // .bottom for divider
        ZStack(alignment: .bottom) {

            content()

            DividerBgScroll(scrollToHeader: scrollToHeader)
        }
        .safeAreaPadding(.top)
    }
}

struct Fs__HeaderTitle: View {

    let title: String
    let scrollToHeader: Int
    let onClose: () -> Void

    var body: some View {

        Fs__Header(
            scrollToHeader: scrollToHeader
        ) {

            HStack {

                HeaderTitle(
                    title: title
                )

                Spacer()

                Fs__CloseButton(
                    onClick: {
                        onClose()
                    }
                )
                .padding(.trailing, H_PADDING)
            }
            .padding(.bottom, 6)
        }
    }
}

struct Fs__HeaderAction: View {

    let title: String
    let actionText: String
    let scrollToHeader: Int
    let onCancel: () -> Void
    let onDone: () -> Void

    private var bgAlpha: Double {
        (Double(scrollToHeader) / 30).limitMinMax(0, 1)
    }

    var body: some View {

        Fs__Header(
            scrollToHeader: scrollToHeader
        ) {

            VStack(alignment: .leading) {

                Button(
                    action: {
                        onCancel()
                    },
                    label: {
                        Text("Cancel")
                            .foregroundColor(c.textSecondary)
                            .font(.system(size: 15, weight: .light))
                            .padding(.leading, H_PADDING)
                    }
                )
                .padding(.bottom, 2)

                HStack {

                    HeaderTitle(
                        title: title
                    )

                    Spacer()

                    Button(
                        action: {
                            onDone()
                        },
                        label: {
                            Text(actionText)
                                .foregroundColor(c.text)
                                .font(.system(size: Fs__BUTTON_FONT_SIZE, weight: Fs__TITLE_FONT_WEIGHT))
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(roundedShape.fill(c.blue))
                        }
                    )
                    .padding(.trailing, H_PADDING)
                }
                .padding(.bottom, 6)
            }
            .padding(.leading, halfDpCeil)
        }
    }
}

struct Fs__HeaderClose: View {

    let onClose: () -> Void

    var body: some View {

        HStack {

            Spacer()

            Fs__CloseButton {
                onClose()
            }
            .padding(.trailing, H_PADDING)
        }
        .safeAreaPadding(.top)
        .padding(.top, H_PADDING)
        .zIndex(2)
    }
}

private struct HeaderTitle: View {

    let title: String

    var body: some View {
        Text(title)
            .foregroundColor(c.text)
            .font(.system(size: Fs__TITLE_FONT_SIZE, weight: Fs__TITLE_FONT_WEIGHT))
            .padding(.leading, H_PADDING)
    }
}
