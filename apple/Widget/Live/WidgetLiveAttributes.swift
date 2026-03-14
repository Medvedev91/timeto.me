import Foundation
import ActivityKit

struct WidgetLiveAttributes: ActivityAttributes {
    
    struct ContentState: Codable, Hashable {
        let title: String
        let isTimerOrStopwatch: Bool
        let date: Date
    }
}
