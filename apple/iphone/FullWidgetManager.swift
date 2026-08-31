import Combine
import WidgetKit
import shared

private let fullWidgetPublisher: AnyPublisher<AnyObject, Never> =
    WidgetFlow.shared.flow.toPublisher()

private var keepObject: Any? = nil

class FullWidgetManager {
    
    static func setup() {
        keepObject = fullWidgetPublisher.sink { value in
            Task {
                try await KvDb.KEY.iosWidgetUpdateId.upsertString(value: UUID().uuidString)
                WidgetCenter.shared.reloadAllTimelines()
            }
        }
    }
}
