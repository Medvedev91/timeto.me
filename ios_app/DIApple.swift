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
    @Published var taskFolders = [TaskFolderModel]()
    @Published var tasks = [TaskModel]()
    @Published var lastInterval: IntervalModel? = nil
    @Published var activities = [ActivityModel]()
}

private struct DIApple__Modifier: ViewModifier {

    private typealias pubArray = AnyPublisher<NSArray, Never>

    @StateObject private var diApple = DIApple()

    private let checklistItems: pubArray = ChecklistItemModel.Companion().getAscFlow().toPublisher()
    private let taskFolders: pubArray = TaskFolderModel.Companion().getAscBySortFlow().toPublisher()
    private let tasks: pubArray = TaskModel.Companion().getAscFlow().toPublisher()
    private let lastInterval: AnyPublisher<IntervalModel, Never> = IntervalModel.Companion().getLastOneOrNullFlow().toPublisher()
    private let activities: pubArray = ActivityModel.Companion().getAscSortedFlow().toPublisher()

    func body(content: Content) -> some View {
        content
                .onReceive(checklistItems) { diApple.checklistItems = $0 as! [ChecklistItemModel] }
                .onReceive(taskFolders) { diApple.taskFolders = $0 as! [TaskFolderModel] }
                .onReceive(tasks) { diApple.tasks = $0 as! [TaskModel] }
                .onReceive(lastInterval) { diApple.lastInterval = $0 as! IntervalModel? }
                .onReceive(activities) { diApple.activities = $0 as! [ActivityModel] }
                .environmentObject(diApple)
    }
}
