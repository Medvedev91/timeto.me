import SwiftUI

///
/// You probably want to change this struct

private struct TimetoSheetFullscreen: View {

    let wrapper: TimetoSheetWrapper
    @Binding var isPresented: Bool
    @State var isShown = false

    var body: some View {
        ZStack(alignment: .bottom) {

            if isShown {
                Color(.black)
                        .transition(.opacity)
                        .opacity(0.5)
                        .ignoresSafeArea()
                        .onTapGesture {
                            isPresented = false
                        }
            }

            ZStack {
                if isShown {
                    wrapper
                            .content()
                            .transition(.move(edge: .bottom))
                }
            }
        }
                .ignoresSafeArea()
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                .onAppear {
                    withAnimation(.spring(response: 0.250)) {
                        isShown = true
                    }
                }
                .onChange(of: isPresented) { _ in
                    withAnimation(.spring(response: 0.250)) {
                        isShown = isPresented
                    }
                }
    }
}

///
///

private class TimetoSheetWrappers: ObservableObject {
    @Published var wrappers = [TimetoSheetWrapper]()
}

private struct TimetoSheetModifier: ViewModifier {

    @StateObject private var wrappers = TimetoSheetWrappers()

    func body(content: Content) -> some View {
        ZStack {
            content
            ForEach(wrappers.wrappers) { wrapper in
                TimetoSheetFullscreen(wrapper: wrapper, isPresented: wrapper.$isPresented)
            }
        }
                .environmentObject(wrappers)
    }
}

private struct TimetoSheetWrapper: View, Identifiable {

    @EnvironmentObject private var wrappers: TimetoSheetWrappers

    @Binding var isPresented: Bool
    @ViewBuilder let content: () -> AnyView
    @ViewBuilder let parent: () -> AnyView

    @State var id = UUID().uuidString

    var body: some View {
        ZStack {
            parent()
                    .onChange(of: isPresented) { _ in
                        if isPresented && wrappers.wrappers.filter { $0.id == id }.isEmpty {
                            wrappers.wrappers.append(self)
                        }
                    }
                    .onDisappear {
                        wrappers.wrappers.removeAll { $0.id == id }
                    }
        }
    }
}

extension View {

    func attachTimetoSheet() -> some View {
        modifier(TimetoSheetModifier())
    }

    func timetoSheet<Content: View>(
            isPresented: Binding<Bool>,
            @ViewBuilder content: @escaping () -> Content
    ) -> some View {
        TimetoSheetWrapper(
                isPresented: isPresented,
                content: { AnyView(content()) },
                parent: { AnyView(self) }
        )
    }
}

///
/// https://stackoverflow.com/a/58606176/5169420

private struct TimetoSheetRoundedCorner: Shape {

    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}

extension View {

    func timeToSheetCornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(TimetoSheetRoundedCorner(radius: radius, corners: corners))
    }
}
