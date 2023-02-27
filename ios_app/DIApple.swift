import SwiftUI
import Combine
import shared

extension View {

    func attachDIApple() -> some View {
        modifier(DIApple__Modifier())
    }
}

class DIApple: ObservableObject {

    @Published var lastInterval: IntervalModel? = nil
}

private struct DIApple__Modifier: ViewModifier {

    @StateObject private var diApple = DIApple()

    private let lastInterval: AnyPublisher<IntervalModel, Never> = IntervalModel.companion.getLastOneOrNullFlow().toPublisher()

    func body(content: Content) -> some View {
        content
                .onReceive(lastInterval) { diApple.lastInterval = $0 as! IntervalModel? }
                .environmentObject(diApple)
    }
}
