import SwiftUI

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
                content($isPresented)
                    .transition(.opacity)
                    .ignoresSafeArea()
                    .onDisappear {
                        fs.items.removeAll {
                            $0.id == id
                        }
                    }
            }
        }
        .animation(fsAnimation, value: isPresented)
        .ignoresSafeArea()
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
        .onAppear {
            isPresented = true
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
