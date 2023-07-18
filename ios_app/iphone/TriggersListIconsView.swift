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
                    .padding(.leading, 3)
        }
    }
}
