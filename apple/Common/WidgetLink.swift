import SwiftUI

enum WidgetLink {
    
    static func parse(_ url: URL) -> WidgetLink? {
        guard let host: String = url.host else {
            reportApi("WidgetLink.parse() no host in \(url)")
            return nil
        }
        if host == "toggle" {
            return .Toggle
        }
        reportApi("WidgetLink.parse() nil in \(url)")
        return nil
    }
    
    func buildUrl() -> URL {
        let path: String = switch self {
        case .Toggle: "toggle"
        }
        return URL(string: "timeto.me://\(path)")!
    }
    
    ///
    
    case Toggle
}
