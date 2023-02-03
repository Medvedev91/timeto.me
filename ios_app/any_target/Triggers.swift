import SwiftUI
import shared

struct Triggers__Parsed {

    let text: String
    let triggers: [Trigger]

    init(
            _ text: String,
            ensureInDb: Bool = true
    ) {
        var newText = text
        var triggers = [Trigger]()

        /// Checklists
        matches(for: "#c(\\d{10})", in: newText).forEach { plainId in
            if !ensureInDb {
                newText = newText.replacingOccurrences(of: plainId, with: "")
                return
            }
            let id = Int(plainId.filter("0123456789.".contains))!
            if let checklist = DI.checklists.filter { $0.id == id }.first {
                triggers.append(Trigger.Checklist(checklist: checklist))
                newText = newText.replacingOccurrences(of: plainId, with: "")
            }
        }

        /// Shortcuts
        matches(for: "#s(\\d{10})", in: newText).forEach { plainId in
            if !ensureInDb {
                newText = newText.replacingOccurrences(of: plainId, with: "")
                return
            }
            let id = Int(plainId.filter("0123456789.".contains))!
            if let shortcut = DI.shortcuts.first { $0.id.toInt() == id } {
                triggers.append(Trigger.Shortcut(shortcut: shortcut))
                newText = newText.replacingOccurrences(of: plainId, with: "")
            }
        }

        // todo remove duplicate spaces by kmm
        self.text = newText.trim()
        self.triggers = triggers
    }
}
