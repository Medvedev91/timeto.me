import SwiftUI
import shared

struct EventTemplatesView: View {

    let spaceAround: Double
    let paddingTop: Double
    let onClick: (EventsHistory.Item) -> Void

    @State private var vm = EventTemplatesVM()

    var body: some View {

        VMView(vm: vm, stack: .ZStack()) { state in

            if !state.uiItems.isEmpty {

                ScrollView(.horizontal, showsIndicators: false) {

                    HStack(spacing: 0) {

                        MySpacerSize(width: spaceAround)

                        ForEach(state.uiItems, id: \.historyItem.normalized_title) { uiItem in

                            MySpacerSize(width: state.uiItems.first !== uiItem ? 8 : 0)

                            Button(
                                    action: {
                                        // In onTapGesture()/onLongPressGesture()
                                    },
                                    label: {
                                        Text(uiItem.note)
                                                .padding(.vertical, 6)
                                                .padding(.horizontal, 11)
                                                .background(Capsule(style: .circular).fill(.blue))
                                                /// Ordering is important
                                                .onTapGesture {
                                                    onClick(uiItem.historyItem)
                                                }
                                                .onLongPressGesture(minimumDuration: 0.1) {
                                                    vibrate(.warning)
                                                    vm.delItem(item: uiItem.historyItem)
                                                }
                                                //////
                                                .foregroundColor(.white)
                                                .font(.system(size: 14, weight: .semibold))
                                    }
                            )
                        }

                        MySpacerSize(width: spaceAround)
                    }
                }
                        .padding(.top, paddingTop)
            }
        }
    }
}
