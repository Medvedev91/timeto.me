import SwiftUI
import shared

struct ActivityEmojiPickerView: View {

    @State private var vm = ActivityEmojiPickerVM()

    var text: String
    var spaceAround: CGFloat
    var onSelect: (/* new string */ String) -> Void

    var body: some View {

        VMView(vm: vm, stack: .VStack(spacing: 0)) { state in

            ScrollView(.horizontal, showsIndicators: false) {

                HStack(spacing: 0) {

                    MySpacerSize(width: spaceAround)

                    ForEach(state.activities, id: \.id) { activity in
                        Button(
                                action: {
                                    onSelect(vm.upText(text: text, activity: activity))
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
