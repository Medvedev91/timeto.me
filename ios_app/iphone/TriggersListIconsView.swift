import SwiftUI
import UIKit
import shared

struct TriggersListIconsView: View {

    let triggers: [TextFeatures.Trigger]
    let fontSize: CGFloat

    var body: some View {
        if triggers.isEmpty {
            EmptyView()
        } else {
            HStack {
                ForEach(triggers, id: \.id) { trigger in
                    TriggerIcon(trigger: trigger, fontSize: fontSize)
                }
            }
                    .padding(.leading, 3)
        }
    }
}

private struct TriggerIcon: View {

    let trigger: TextFeatures.Trigger
    let fontSize: CGFloat

    var body: some View {
        Button(
                action: {
                    trigger.performUI()
                },
                label: {
                    Text(trigger.emoji)
                            .font(.system(size: fontSize))
                            .padding(.horizontal, 3)
                }
        )
    }
}
