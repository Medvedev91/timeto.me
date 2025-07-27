import Foundation
import ActivityKit

struct WidgetAttributes: ActivityAttributes {
    
    struct ContentState: Codable, Hashable {
        let title: String
        let endDate: Date
    }
}
