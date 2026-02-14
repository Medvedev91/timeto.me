import Foundation
import ActivityKit

struct WidgetLiveAttributes: ActivityAttributes {
    
    struct ContentState: Codable, Hashable {
        let title: String
        let endDate: Date
    }
}
