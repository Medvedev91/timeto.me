import SwiftUI
import shared

struct ActivityEmojiPickerView: View {

    @EnvironmentObject private var diApple: DIApple

    var text: String
    var spaceAround: CGFloat
    var onSelect: (/* emoji */ String, /* new string */ String) -> Void

    var body: some View {

        VStack(spacing: 0) {

            ScrollView(.horizontal, showsIndicators: false) {

                HStack(spacing: 0) {

                    MySpacerSize(width: spaceAround)

                    let allActivities = diApple.activities

                    ForEach(allActivities, id: \.id) { activity in
                        Button(
                                action: {
                                    let textFirstEmoji = allActivities
                                            .map { ($0.emoji, text.index(of: $0.emoji)) }
                                            .filter { $0.1 != nil }
                                            .min(by: { $0.1! < $1.1! })?
                                            .0

                                    let newText: String
                                    if let textFirstEmoji = textFirstEmoji {
                                        newText = text.replacingOccurrences(of: textFirstEmoji, with: activity.emoji)
                                    } else {
                                        newText = "\(text.trim()) \(activity.emoji)"
                                    }

                                    onSelect(activity.emoji, newText)
                                },
                                label: {
                                    Text(activity.emoji)
                                            .font(.system(size: 26))
                                            .padding(5)
                                }
                        )
                    }

                    MySpacerSize(width: spaceAround)
                }
            }
        }
                .frame(maxWidth: .infinity)
    }
}
