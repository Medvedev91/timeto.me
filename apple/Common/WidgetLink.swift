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
        if host == "activity_button" {
            guard let rawActivityId: String = getParameter(url: url.absoluteString, param: "activity_id"),
                  let activityId: Int = Int(rawActivityId) else {
                reportApi("WidgetLink.parse() activity button nil activity id in \(url)")
                return nil
            }
            return .ActivityButton(activityId: activityId)
        }
        if host == "checklist_item" {
            guard let rawItemId: String = getParameter(url: url.absoluteString, param: "item_id"),
                  let itemId: Int = Int(rawItemId) else {
                reportApi("WidgetLink.parse() checklist item id nil in \(url)")
                return nil
            }
            return .ChecklistItem(itemId: itemId)
        }
        if host == "task_folder" {
            guard let rawTaskFolderId: String = getParameter(url: url.absoluteString, param: "task_folder_id"),
                  let taskFolderId: Int = Int(rawTaskFolderId) else {
                reportApi("WidgetLink.parse() task folder id nil in \(url)")
                return nil
            }
            return .TaskFolder(taskFolderId: taskFolderId)
        }
        reportApi("WidgetLink.parse() nil in \(url)")
        return nil
    }
    
    func buildUrl() -> URL {
        let path: String = switch self {
        case .Toggle:
            "toggle"
        case .ActivityButton(let activityId):
            "activity_button?activity_id=\(activityId)"
        case .ChecklistItem(let itemId):
            "checklist_item?item_id=\(itemId)"
        case .TaskFolder(let taskFolderId):
            "task_folder?task_folder_id=\(taskFolderId)"
        }
        return URL(string: "timeto.me://\(path)")!
    }
    
    ///
    
    case Toggle
    case ActivityButton(activityId: Int)
    case ChecklistItem(itemId: Int)
    case TaskFolder(taskFolderId: Int)
}

private func getParameter(url: String, param: String) -> String? {
    guard let url = URLComponents(string: url) else {
        return nil
    }
    return url.queryItems?.first(where: { $0.name == param })?.value
}
