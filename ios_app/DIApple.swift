import SwiftUI
import Combine
import shared

extension View {

    func attachDIApple() -> some View {
        modifier(DIApple__Modifier())
    }
}

class DIApple: ObservableObject {

    @Published var checklistItems = [ChecklistItemModel]()
    @Published var lastInterval: IntervalModel? = nil
}

private struct DIApple__Modifier: ViewModifier {

    private typealias pubArray = AnyPublisher<NSArray, Never>

    @StateObject private var diApple = DIApple()

    private let checklistItems: pubArray = ChecklistItemModel.companion.getAscFlow().toPublisher()
    private let lastInterval: AnyPublisher<IntervalModel, Never> = IntervalModel.companion.getLastOneOrNullFlow().toPublisher()

    func body(content: Content) -> some View {
        content
                .onReceive(checklistItems) { diApple.checklistItems = $0 as! [ChecklistItemModel] }
                .onReceive(lastInterval) { diApple.lastInterval = $0 as! IntervalModel? }
                .environmentObject(diApple)
    }
}
