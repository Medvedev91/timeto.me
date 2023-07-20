import SwiftUI
import UIKit
import shared

private let emojiSpacing = 6.0

struct TriggersListIconsView: View {

    let triggers: [TextFeatures.Trigger]
    let fontSize: CGFloat
    var leadingPadding: CGFloat = emojiSpacing

    var body: some View {
        if triggers.isEmpty {
            EmptyView()
        } else {
            HStack(spacing: emojiSpacing) {
                ForEach(triggers, id: \.id) { trigger in
                    Button(
                            action: {
                                trigger.performUI()
                            },
                            label: {
                                Text(trigger.emoji)
                                        .font(.system(size: fontSize))
                            }
                    )
                }
            }
                    .padding(.leading, leadingPadding)
        }
    }
}
