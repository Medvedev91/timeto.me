import SwiftUI

class TimetoSheet: ObservableObject {
    @Published var items = [TimetoSheet__Item<AnyView>]()
}

extension View {

    func attachTimetoSheet() -> some View {
        modifier(TimetoSheet__Modifier())
    }
}

///
///

struct TimetoSheet__Item<Content: View>: View, Identifiable {

    @EnvironmentObject private var timetoSheet: TimetoSheet

    @Binding var isPresented: Bool
    @State var isShown = false

    @ViewBuilder var content: () -> Content

    let id = UUID().uuidString

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
                    content()
                            .padding(.top, 10.0)
                            .safeAreaPadding(.top)
                            .transition(.move(edge: .bottom))
                            .onDisappear {
                                timetoSheet.items.removeAll { $0.id == id }
                            }
                }
            }
        }
                .ignoresSafeArea()
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
                .onAppear {
                    withAnimation(.spring(response: 0.150)) {
                        isPresented = true
                    }
                }
                .onChange(of: isPresented) { newValue in
                    withAnimation(.spring(response: 0.150)) {
                        isShown = newValue
                    }
                }
    }
}

private struct TimetoSheet__Modifier: ViewModifier {

    @StateObject private var timetoSheet = TimetoSheet()

    func body(content: Content) -> some View {
        ZStack {
            content
            ForEach(timetoSheet.items) { wrapper in
                wrapper
            }
        }
                .colorScheme(.dark)
                .environmentObject(timetoSheet)
    }
}
